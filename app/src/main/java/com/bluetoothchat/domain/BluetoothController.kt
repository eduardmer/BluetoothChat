package com.bluetoothchat.domain

import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface BluetoothController {

    val connectionState: SharedFlow<ConnectionResult>

    val isDeviceConnected: Flow<ConnectionResult>

    fun startDiscovery(): Flow<DiscoveryResult>

    fun stopDiscovery()

    fun openServer(): Flow<ConnectionResult>

    fun stopServer()

    suspend fun connectToDevice(device: BluetoothDevice)

}