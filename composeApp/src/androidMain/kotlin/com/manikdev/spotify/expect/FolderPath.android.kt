package com.manikdev.spotify.expect

import android.os.Environment

actual fun getDownloadFolderPath(): String =
    Environment
        .getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS,
        ).path