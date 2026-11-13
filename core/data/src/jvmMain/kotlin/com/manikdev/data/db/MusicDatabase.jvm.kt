package com.manikdev.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import com.manikdev.common.DB_NAME
import com.manikdev.data.io.getHomeFolderPath
import java.io.File

actual fun getDatabaseBuilder(
    converters: Converters
): RoomDatabase.Builder<MusicDatabase> {
    return Room.databaseBuilder<MusicDatabase>(
        name = getDatabasePath()
    ).addTypeConverter(converters)
}

actual fun getDatabasePath(): String {
    val dbFile = File(getHomeFolderPath(listOf(".spotify", "db")), DB_NAME)
    return dbFile.absolutePath
}