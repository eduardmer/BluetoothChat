package com.bluetoothchat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("SELECT * FROM MessagesEntity WHERE remote_device = :deviceAddress")
    fun getMessages(deviceAddress: String): Flow<List<MessagesEntity>>

    @Query("SELECT *, MAX(date) FROM MessagesEntity GROUP BY remote_device")
    fun getLastMessageForEachDevice(): Flow<List<MessagesEntity>>

    @Query("SELECT * FROM MessagesEntity")
    fun getAllMessages(): Flow<List<MessagesEntity>>

    @Insert
    suspend fun saveMessage(message: MessagesEntity)

}