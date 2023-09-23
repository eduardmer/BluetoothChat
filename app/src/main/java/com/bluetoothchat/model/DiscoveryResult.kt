package com.bluetoothchat.model

sealed interface DiscoveryResult {
    object DiscoveryStarted : DiscoveryResult
    data class DiscoveryFinished(val devices: List<BluetoothDevice>) : DiscoveryResult
    data class DiscoveryError(val error: com.bluetoothchat.model.DiscoveryError) : DiscoveryResult
}