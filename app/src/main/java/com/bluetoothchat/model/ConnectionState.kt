package com.bluetoothchat.model

sealed interface ConnectionState {
    object ConnectionInitiated : ConnectionState
    object Disconnected : ConnectionState
    data class ConnectionError(val error: Throwable) : ConnectionState
    data class ConnectionAccepted(val device: BluetoothDevice) : ConnectionState
}