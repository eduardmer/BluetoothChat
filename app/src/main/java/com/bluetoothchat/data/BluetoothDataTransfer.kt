package com.bluetoothchat.data

import android.bluetooth.BluetoothSocket
import com.bluetoothchat.data.local.MessagesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BluetoothDataTransfer(
    private val socket: BluetoothSocket,
    private val localDevice: String,
    ) {

    private val buffer: ByteArray = ByteArray(1024)

    fun listenForMessages() = flow {
        while (true) {
            val byteCount = try {
                socket.inputStream.read(buffer)
            } catch (error: Exception){
                error.printStackTrace()
                throw error
            }
            emit(MessagesEntity(local_device = localDevice, remote_device = socket.remoteDevice.address, message = buffer.decodeToString(endIndex = byteCount), isFromLocal = false, date = System.currentTimeMillis()))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(message: String): MessagesEntity {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(message.encodeToByteArray())
            } catch (error: Exception) {
                error.printStackTrace()
                throw error
            }
            MessagesEntity(local_device = localDevice, remote_device = socket.remoteDevice.address, message = message, isFromLocal = true, date = System.currentTimeMillis())
        }
    }

}