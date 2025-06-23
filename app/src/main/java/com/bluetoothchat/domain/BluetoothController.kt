package com.bluetoothchat.domain

import com.bluetoothchat.data.BluetoothDataTransfer
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val scanningState: StateFlow<ScanningState>
    val connectionState: StateFlow<ConnectionState>
    var dataTransferService: BluetoothDataTransfer?

    fun startDiscovery()

    fun stopDiscovery()

    suspend fun openServer()

    fun closeConnection()

    suspend fun connectToDevice(device: BluetoothDevice)

    fun listenForMessages(): Flow<MessagesEntity>?

    suspend fun sendMessage(message: String): MessagesEntity?

    fun deleteError()

}