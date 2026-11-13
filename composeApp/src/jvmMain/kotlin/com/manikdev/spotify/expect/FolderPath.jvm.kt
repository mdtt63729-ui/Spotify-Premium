package com.manikdev.spotify.expect

actual fun getDownloadFolderPath(): String = System.getProperty("user.home") + "/Downloads"