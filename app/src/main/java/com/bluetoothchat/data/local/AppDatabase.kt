package com.bluetoothchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [MessagesEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messagesDao(): MessagesDao

}