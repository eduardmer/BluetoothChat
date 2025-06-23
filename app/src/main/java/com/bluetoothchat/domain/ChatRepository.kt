package com.bluetoothchat.domain

import com.bluetoothchat.data.local.MessagesEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getLastMessageForEachDevice(): Flow<List<MessagesEntity>>

    fun getMessages(deviceAddress: String): Flow<List<MessagesEntity>>

    suspend fun saveMessage(message: MessagesEntity)

}