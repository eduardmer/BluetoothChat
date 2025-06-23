package com.bluetoothchat.domain

import com.bluetoothchat.model.BluetoothDevice

data class ScanningState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val hasScannedBefore: Boolean = false,
    val isScanning: Boolean = false,
    val error: String? = null
)
