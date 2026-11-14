package com.manikdev.spotify.recommendation

import com.manikdev.kotlinytmusicscraper.YouTube
import com.manikdev.kotlinytmusicscraper.models.SongItem
import com.manikdev.kotlinytmusicscraper.models.WatchEndpoint
import com.maxrave.logger.Logger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Echo-style home recommendation engine.
 *
 * Port of the Echo Music engine described in the PRD: no model, no embeddings, no crawler.
 * Every song comes from a structured InnerTube call this app already makes, and the only
 * client-side work is the selection step — priority interleaving plus artwork de-duplication.
 *
 * The problem it solves is the cold start. On a fresh install the app has no play history,
 * no liked songs and no library, so every personalised shelf is empty and Home looks dead.
 * This engine fills that hole from upstream data alone, in tiers, emitting a partial result
 * after each tier so the first screen paints within a second instead of waiting for the
 * slowest radio lookup.
 *
 *   Tier 1  new releases   - explore() albums expanded into their songs
 *   Tier 2  radio          - six seeds spread across the pool, each expanded through next()
 *   Tier 3  radio (deep)   - a second round seeded from the songs tier 2 discovered
 *
 * The merged pool then goes through [selectHeroCarouselSongs], the same selector a warm
 * (history-driven) carousel uses, so ordering, artwork diversity and the overflow tail behave
 * identically. Only the limit is larger — 40 cards instead of 20.
 */
class EchoRecommendationEngine(
    private val youTube: YouTube,
    /** The user's own songs (liked / in library), as the selector's middle priority band. */
    private val library: List<SongItem> = emptyList(),
    /** Most recent plays, newest first, as the selector's top priority band. */
    private val recentlyPlayed: List<SongItem> = emptyList(),
    private val hideExplicit: Boolean = false,
    private val hideVideoSongs: Boolean = false,
    private val hiddenSongIds: Set<String> = emptySet(),
) {
    companion object {
        /** Cards produced on a cold start. Deliberately larger than a warm start's 20. */
        const val TARGET_CARDS = 60

        /** Below this the caller should keep the upstream shelves only. */
        const val MIN_CARDS = 12

        /** Songs used as radio seeds per round. */
        const val RADIO_SEEDS = 6

        /** How many new-release albums are expanded. */
        const val ALBUM_BUDGET = 3

        const val LOOKUP_TIMEOUT_MS = 15_000L
        const val MAX_PARALLEL = 4
    }

    private val semaphore = Semaphore(MAX_PARALLEL)

    /**
     * Builds the cold-start carousel.
     *
     * @param onProgress called after every tier with the current best list, so the caller can
     *        paint early instead of waiting for the last radio lookup. Called at most three times.
     */
    suspend fun build(
        onProgress: suspend (List<SongItem>) -> Unit = {},
    ): List<SongItem> {
        val pool = LinkedHashMap<String, SongItem>()

        // Tier 1 — new releases, expanded into their songs.
        merge(pool, newReleaseSongs())
        onProgress(select(pool.values))
        if (pool.isEmpty()) return emptyList()

        // Tier 2 — radio expansion from seeds spread across the pool.
        merge(pool, radioSongs(pool.values.toList(), RADIO_SEEDS))
        onProgress(select(pool.values))

        // Tier 3 — a second round seeded from what tier 2 discovered, so the pool keeps growing
        // instead of circling the same three albums.
        val tier2 = pool.values.toList()
        merge(pool, radioSongs(tier2.drop(tier2.size / 2), RADIO_SEEDS))
        val final = select(pool.values)
        onProgress(final)
        return final
    }

    // ---------------------------------------------------------------------------------
    // Tiers
    // ---------------------------------------------------------------------------------

    /** New releases from explore(), expanded into their songs. One call plus one per album. */
    private suspend fun newReleaseSongs(): List<SongItem> {
        val albums =
            youTube
                .newRelease()
                .getOrNull()
                ?.released
                .orEmpty()
                .take(ALBUM_BUDGET)
        if (albums.isEmpty()) return emptyList()

        return coroutineScope {
            albums
                .map { album ->
                    async {
                        semaphore.withPermit {
                            withTimeoutOrNull(LOOKUP_TIMEOUT_MS) {
                                youTube.album(album.browseId).getOrNull()?.songs
                            }.orEmpty()
                        }
                    }
                }.awaitAll()
                .flatten()
                .filter(::isAllowed)
                .distinctBy { it.id }
        }
    }

    /**
     * The tier that turns "some songs" into "a lot of songs": each seed is expanded through
     * next(), which is the same call the player makes for a radio queue.
     */
    private suspend fun radioSongs(
        seeds: List<SongItem>,
        count: Int,
    ): List<SongItem> {
        if (seeds.isEmpty()) return emptyList()

        // Spread the seeds across the pool instead of taking six songs from one album.
        val stride = (seeds.size / count).coerceAtLeast(1)
        val chosen = seeds.filterIndexed { index, _ -> index % stride == 0 }.take(count)
        if (chosen.isEmpty()) return emptyList()

        return coroutineScope {
            chosen
                .map { seed ->
                    async {
                        semaphore.withPermit {
                            withTimeoutOrNull(LOOKUP_TIMEOUT_MS) {
                                youTube
                                    .next(WatchEndpoint(videoId = seed.id))
                                    .getOrNull()
                                    ?.items
                                    .orEmpty()
                                    .filter { it.id != seed.id }
                            }.orEmpty()
                        }
                    }
                }.awaitAll()
                .flatten()
                .filter(::isAllowed)
                .distinctBy { it.id }
        }
    }

    // ---------------------------------------------------------------------------------
    // Selection + helpers
    // ---------------------------------------------------------------------------------

    /** Inserts songs into the pool without overwriting an earlier, higher-tier copy. */
    private fun merge(
        pool: MutableMap<String, SongItem>,
        songs: List<SongItem>,
    ) {
        songs.forEach { song -> pool.putIfAbsent(song.id, song) }
    }

    /** Cold start has no history by definition, so only the recommendation pool is populated. */
    private fun select(candidates: Collection<SongItem>): List<SongItem> =
        selectHeroCarouselSongs(
            recentlyPlayed = recentlyPlayed,
            library = library,
            recommendations = candidates.toList(),
            account = emptyList(),
            excludedSongIds = hiddenSongIds,
            blockedArtists = emptySet(),
            hideExplicit = hideExplicit,
            hideVideoSongs = hideVideoSongs,
            limit = TARGET_CARDS,
        )

    private fun isAllowed(song: SongItem): Boolean {
        if (song.id in hiddenSongIds) return false
        if (hideExplicit && song.explicit) return false
        if (hideVideoSongs && isVideoSong(song)) return false
        return true
    }
}

/** True when the track is a video rendition rather than the audio ("ATV") recording. */
fun isVideoSong(song: SongItem): Boolean {
    val type = song.musicVideoType ?: return false
    return type != "MUSIC_VIDEO_TYPE_ATV"
}

/**
 * The hero-carousel selector, ported from Echo Music unchanged.
 *
 * Priority interleaving first: recently played, then the library/account pair interleaved,
 * then recommendations — so a user with a large library still sees discovery instead of twenty
 * library tracks in a row.
 *
 * Then artwork de-duplication: two songs from the same album share an album id and usually a
 * thumbnail, and two cards with identical artwork look like a bug. A song that collides is not
 * dropped — it is appended after the diverse ones, so the carousel still reaches its limit.
 */
fun selectHeroCarouselSongs(
    recentlyPlayed: List<SongItem>,
    library: List<SongItem>,
    recommendations: List<SongItem>,
    account: List<SongItem> = emptyList(),
    excludedSongIds: Set<String> = emptySet(),
    blockedArtists: Set<String> = emptySet(),
    hideExplicit: Boolean = false,
    hideVideoSongs: Boolean = false,
    limit: Int = 20,
): List<SongItem> {
    if (limit <= 0) return emptyList()

    val sources =
        listOf(recentlyPlayed, interleaveSongs(listOf(library, account)), recommendations).map { songs ->
            songs
                .filterNot { it.id in excludedSongIds }
                .filterNot { song -> song.artists.any { it.name in blockedArtists } }
                .filterNot { hideExplicit && it.explicit }
                .filterNot { hideVideoSongs && isVideoSong(it) }
                .distinctBy { it.id }
        }
    val candidates = interleaveSongs(sources).distinctBy { it.id }

    val artwork = mutableSetOf<String>()
    val selected = mutableListOf<SongItem>()
    val repeatedArtwork = mutableListOf<SongItem>()
    for (song in candidates) {
        val artworkKeys =
            listOfNotNull(
                song.album?.id?.let { "album:$it" },
                song.thumbnail
                    .substringBefore('?')
                    .substringBefore('=')
                    .takeIf { it.isNotBlank() }
                    ?.let { "art:$it" },
            )
        if (artworkKeys.none { it in artwork }) {
            artwork.addAll(artworkKeys)
            selected.add(song)
            if (selected.size == limit) return selected
        } else {
            repeatedArtwork.add(song)
        }
    }
    return (selected + repeatedArtwork).take(limit)
}

/** Round-robin across the sources: a[0], b[0], c[0], a[1], b[1], c[1], … skipping exhausted lists. */
private fun interleaveSongs(sources: List<List<SongItem>>) =
    buildList {
        val longest = sources.maxOfOrNull { it.size } ?: 0
        for (index in 0 until longest) {
            sources.forEach { source -> source.getOrNull(index)?.let { add(it) } }
        }
    }
