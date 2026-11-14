package com.manikdev.spotify.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.maxrave.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * In-app updater for the GitHub release channel.
 *
 * Everything happens inside the app: check the release feed, download the APK for this device's
 * ABI into app storage, then hand it to the system installer. No browser, no Play Store.
 *
 * The ABI match is strict: every entry in [Build.SUPPORTED_ABIS] is tried in order and the
 * universal APK is never picked, so the download is always the smallest binary that actually
 * runs on this device.
 *
 * Two states are persisted, and they are what make the sheet behave the way it should:
 *  - a downloaded-but-not-installed APK is remembered by version, so the next launch offers
 *    "Install update" instead of downloading the same file again;
 *  - when a *newer* release appears, the stored APK is deleted and the sheet goes back to
 *    offering a download for the new version.
 *
 * Old APKs are cleaned up on every check, so the app never accumulates installers it has
 * already moved past.
 */
class AndroidUpdateController(
    private val context: Context,
    private val currentVersion: String,
    private val repo: String = "mdtt63729-ui/Spotify-Premium",
) : UpdateHostController {
    companion object {
        private const val PREFS = "app_update"
        private const val KEY_VERSION = "pending_version"
        private const val KEY_PATH = "pending_path"
        private const val KEY_NOTES = "pending_notes"
        private const val TAG = "AndroidUpdateController"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    override val state: StateFlow<UpdateUiState> = _state

    /** Called once per process start: resumes a pending install or looks for a new release. */
    fun start() {
        scope.launch { refresh() }
    }

    override fun check() {
        scope.launch { refresh() }
    }

    override fun download() {
        val info = (_state.value as? UpdateUiState.Available)?.info
            ?: (_state.value as? UpdateUiState.Failed)?.info
            ?: return
        scope.launch { downloadInternal(info) }
    }

    override fun install() {
        val info = (_state.value as? UpdateUiState.ReadyToInstall)?.info ?: return
        val path = prefs.getString(KEY_PATH, null) ?: return
        val file = File(path)
        if (!file.exists()) {
            clearPending()
            _state.value = UpdateUiState.Idle
            return
        }
        // Installing an APK from inside the app needs the user's blessing once per install source.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            runCatching {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            return
        }
        runCatching {
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.FileProvider",
                    file,
                )
            context.startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.onFailure { Logger.w(TAG, "install failed: ${it.message}") }
        // The installer takes over from here; the APK is removed on the next check once the
        // version on disk is no longer the newest one.
    }

    override fun dismiss() {
        _state.value = UpdateUiState.Idle
    }

    // ---------------------------------------------------------------------------------

    private suspend fun refresh() {
        if (_state.value !is UpdateUiState.ReadyToInstall) {
            _state.value = UpdateUiState.Checking
        }
        // 1. A downloaded APK from an earlier session wins, unless the feed already moved on.
        val pendingVersion = prefs.getString(KEY_VERSION, null)
        val pendingPath = prefs.getString(KEY_PATH, null)
        val latest = runCatching { fetchLatest() }.getOrNull()

        if (pendingVersion != null && pendingPath != null) {
            val file = File(pendingPath)
            if (!file.exists()) {
                clearPending()
            } else if (latest == null || compareVersions(pendingVersion, latest.versionName) >= 0) {
                // Still the newest thing we know about: offer Install, never Download.
                _state.value =
                    UpdateUiState.ReadyToInstall(
                        UpdateInfo(
                            versionName = pendingVersion,
                            notes = prefs.getString(KEY_NOTES, null)?.split("\n").orEmpty(),
                            sizeBytes = file.length(),
                        ),
                    )
                return
            } else {
                file.delete()
                clearPending()
            }
        }

        if (latest == null) {
            if (_state.value is UpdateUiState.Checking) _state.value = UpdateUiState.Idle
            return
        }
        if (compareVersions(currentVersion, latest.versionName) >= 0) {
            cleanOldApks(keep = null)
            if (_state.value is UpdateUiState.Checking) _state.value = UpdateUiState.Idle
            return
        }
        cleanOldApks(keep = null)
        _state.value = UpdateUiState.Available(latest)
    }

    private suspend fun downloadInternal(info: UpdateInfo) {
        val target = File(context.cacheDir, "update-${info.versionName}.apk")
        _state.value = UpdateUiState.Downloading(info, 0f)
        withContext(Dispatchers.IO) {
            runCatching {
                val connection = (URL(info.apkUrl).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    setRequestProperty("Accept", "application/vnd.github+json")
                }
                connection.connect()
                val total = connection.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var read: Int
                        var written = 0L
                        while (input.read(buffer).also { read = it } > 0) {
                            output.write(buffer, 0, read)
                            written += read
                            if (total > 0) {
                                _state.value =
                                    UpdateUiState.Downloading(
                                        info = info,
                                        progress = (written.toFloat() / total).coerceIn(0f, 1f),
                                    )
                            }
                        }
                    }
                }
                connection.disconnect()
                target
            }.onSuccess { file ->
                prefs
                    .edit()
                    .putString(KEY_VERSION, info.versionName)
                    .putString(KEY_PATH, file.absolutePath)
                    .putString(KEY_NOTES, info.notes.joinToString("\n"))
                    .apply()
                cleanOldApks(keep = file)
                _state.value = UpdateUiState.ReadyToInstall(info)
            }.onFailure {
                Logger.w(TAG, "download failed: ${it.message}")
                runCatching { target.delete() }
                _state.value = UpdateUiState.Failed(info, "Download failed. Check your connection and try again.")
            }
        }
    }

    /** Reads the newest release from GitHub: version, changelog lines and the APK for this ABI. */
    private fun fetchLatest(): UpdateInfo? {
        val connection =
            (URL("https://api.github.com/repos/$repo/releases/latest").openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 15_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Spotify-Android")
            }
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val json = JSONObject(body)
        val tag = json.optString("tag_name").removePrefix("v")
        if (tag.isBlank()) return null

        val notes =
            json
                .optString("body")
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { line ->
                    when {
                        line.startsWith("*") || line.startsWith("-") -> "• " + line.drop(1).trim()
                        else -> line
                    }
                }.toList()

        val assets = json.optJSONArray("assets")
        var url = ""
        var size = 0L
        var abiLabel = ""
        if (assets != null) {
            // Only an ABI-specific build may be installed. The universal APK is deliberately
            // never a candidate — a phone that supports arm64-v8a gets the arm64-v8a APK, one
            // that supports x86_64 gets that, and if this device matches nothing we offer no
            // update at all rather than quietly pushing the fat binary.
            val all = (0 until assets.length()).map { assets.getJSONObject(it) }
            val apks =
                all.filter { asset ->
                    val name = asset.optString("name").lowercase()
                    name.endsWith(".apk") && !name.contains("universal") && !name.contains("vivo")
                }
            val chosen =
                Build.SUPPORTED_ABIS
                    .asSequence()
                    .map { it.lowercase() }
                    .firstNotNullOfOrNull { abi ->
                        apks.firstOrNull { it.optString("name").lowercase().contains(abi) }
                    }
            if (chosen != null) {
                url = chosen.optString("browser_download_url")
                size = chosen.optLong("size")
                abiLabel =
                    listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
                        .firstOrNull { chosen.optString("name").lowercase().contains(it) }
                        .orEmpty()
            }
        }
        if (url.isBlank()) return null
        return UpdateInfo(versionName = tag, notes = notes, apkUrl = url, sizeBytes = size, abiLabel = abiLabel)
    }

    private fun cleanOldApks(keep: File?) {
        runCatching {
            context.cacheDir
                .listFiles { file -> file.name.startsWith("update-") && file.name.endsWith(".apk") }
                ?.forEach { file -> if (keep == null || file.absolutePath != keep.absolutePath) file.delete() }
        }
    }

    private fun clearPending() {
        prefs.edit().clear().apply()
    }

    /** Dotted versions compared number by number, so 2.10.0 sorts above 2.9.0. */
    private fun compareVersions(
        a: String,
        b: String,
    ): Int {
        fun numbers(value: String) =
            value
                .removePrefix("v")
                .substringBefore("-")
                .split(".")
                .map { it.toIntOrNull() ?: 0 }
        val x = numbers(a)
        val y = numbers(b)
        for (index in 0 until maxOf(x.size, y.size)) {
            val diff = x.getOrElse(index) { 0 }.compareTo(y.getOrElse(index) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
