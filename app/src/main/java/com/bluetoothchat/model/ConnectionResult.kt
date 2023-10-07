package com.bluetoothchat.model

sealed interface ConnectionResult {
    object ConnectionInitiated : ConnectionResult
    object Disconnected : ConnectionResult
    data class ConnectionError(val error: Throwable) : ConnectionResult
    data class ConnectionAccepted(val device: BluetoothDevice) : ConnectionResult
}