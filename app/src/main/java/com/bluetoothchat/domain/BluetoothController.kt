package com.bluetoothchat.domain

import com.bluetoothchat.data.BluetoothDataTransfer
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.model.ConnectionState
import com.bluetoothchat.model.ScanningState
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val scanningState: StateFlow<ScanningState>
    val connectionState: StateFlow<ConnectionState>
    var dataTransferService: BluetoothDataTransfer?

    fun startDiscovery()

    fun stopDiscovery()

    suspend fun openServer()

    fun stopServer()

    suspend fun connectToDevice(device: BluetoothDevice)

    suspend fun sendMessage(message: String)

}