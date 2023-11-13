package com.bluetoothchat.domain

import com.bluetoothchat.model.BluetoothDevice

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isServerOpen: Boolean = false,
    val error: String? = null
)
