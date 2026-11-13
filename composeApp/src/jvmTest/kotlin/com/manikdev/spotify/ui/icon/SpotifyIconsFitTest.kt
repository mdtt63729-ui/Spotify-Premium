package com.manikdev.spotify.ui.icon

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The fence around SpotifyIcons: every icon file ends with buildSpotifyIcon(), and every icon it builds
 * sits inside Material's 20×20 live area, so a new or regenerated icon cannot slip past either rule.
 */
class SpotifyIconsFitTest {
    private val iconDir =
        listOf(
            "src/commonMain/kotlin/com/manikdev/spotify/ui/icon",
            "composeApp/src/commonMain/kotlin/com/manikdev/spotify/ui/icon",
        ).map(::File).first { it.isDirectory }

    private val iconFiles =
        iconDir
            .listFiles { file -> file.extension == "kt" && file.name != "SpotifyIcons.kt" }
            .orEmpty()
            .sortedBy { it.name }

    @Test
    fun everyIconFileEndsWithBuildSpotifyIcon() {
        assertTrue(iconFiles.size > 100, "found only ${iconFiles.size} icon files in $iconDir")
        val offenders =
            iconFiles.filter { file ->
                val text = file.readText()
                ".buildSpotifyIcon()" !in text || ".build()" in text
            }
        assertTrue(offenders.isEmpty(), "end these with .buildSpotifyIcon(): ${offenders.map { it.name }}")
    }

    @Test
    fun everyIconSitsInsideTheLiveArea() {
        val limit = 24f * ICON_LIVE_AREA / 2f + 0.02f
        val outside =
            iconFiles.mapNotNull { file ->
                val name = file.nameWithoutExtension
                if (name in KEEP_AS_DRAWN) return@mapNotNull null
                val extent = icon(name).drawnExtent()
                if (extent > limit) "$name reaches $extent" else null
            }
        assertTrue(outside.isEmpty(), "outside the 20×20 live area: $outside")
    }

    @Test
    fun aGlyphInsideTheLiveAreaIsLeftAsGenerated() {
        // Each of these already keeps to the 20×20 square, so none is rebuilt and nothing is enlarged.
        listOf("QueueMusic", "MoreVert", "CommentOutline").forEach { name ->
            assertTrue(icon(name).root.all { it is VectorPath }, "$name was rebuilt")
        }
    }

    @Test
    fun aKeptGlyphIsLeftAsGenerated() {
        // Looked up by file name while KEEP_AS_DRAWN matches the builder's own name, so the two drifting apart fails here.
        KEEP_AS_DRAWN.forEach { name ->
            assertTrue(icon(name).root.all { it is VectorPath }, "$name was rebuilt")
        }
    }

    /** `val SpotifyIcons.X` compiles to a static getX(SpotifyIcons) on the file class XKt. */
    private fun icon(name: String): ImageVector =
        Class
            .forName("com.manikdev.spotify.ui.icon.${name}Kt")
            .getMethod("get$name", SpotifyIcons::class.java)
            .invoke(null, SpotifyIcons) as ImageVector

    /** How far the drawn glyph reaches along either axis: a fitted icon is one group scaled about the centre. */
    private fun ImageVector.drawnExtent(): Float {
        val cx = viewportWidth / 2f
        val cy = viewportHeight / 2f
        return root.maxOf { node ->
            when (node) {
                is VectorPath -> node.pathData.extent(cx, cy) ?: Float.MAX_VALUE
                is VectorGroup ->
                    node.scaleX * node.maxOf { (it as VectorPath).pathData.extent(cx, cy) ?: Float.MAX_VALUE }
            }
        }
    }
}
