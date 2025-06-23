package com.bluetoothchat.data

import com.bluetoothchat.data.local.MessagesDao
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.domain.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val messagesDao: MessagesDao
    ) : ChatRepository {

    override fun getLastMessageForEachDevice(): Flow<List<MessagesEntity>> =
        messagesDao.getLastMessageForEachDevice()

    override fun getMessages(deviceAddress: String) =
        messagesDao.getMessages(deviceAddress)

    override suspend fun saveMessage(message: MessagesEntity) =
        messagesDao.saveMessage(message)

}