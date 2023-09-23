package com.bluetoothchat.data

import android.bluetooth.BluetoothSocket
import com.bluetoothchat.domain.BluetoothMessage
import com.bluetoothchat.domain.toBluetoothMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.lang.Exception

class BluetoothDataTransfer(private val socket: BluetoothSocket) {

    private val buffer: ByteArray = ByteArray(1024)

    fun listenForMessages(): Flow<BluetoothMessage> = flow {
        if (!socket.isConnected)
            return@flow
        while (true) {
            val byteCount = try {
                socket.inputStream.read(buffer)
            } catch (error: Exception) {
                throw error
            }
            emit(buffer.decodeToString(endIndex = byteCount).toBluetoothMessage(false))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (error: Exception) {
                error.printStackTrace()
                return@withContext false
            }
            true
        }
    }

}