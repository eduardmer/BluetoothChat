package com.bluetoothchat.domain

import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

    val discoveryState: SharedFlow<DiscoveryResult>
    val connectionState: SharedFlow<ConnectionResult>

    fun startDiscovery()

    fun stopDiscovery()

    suspend fun openServer()

    fun stopServer()

    suspend fun connectToDevice(device: BluetoothDevice)

}