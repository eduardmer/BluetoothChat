package com.bluetoothchat.model

sealed interface ScanningState {
    object EmptyValue : ScanningState
    data class ScanningInProgress(val devices: List<BluetoothDevice>) : ScanningState
    data class ScanningFinished(val devices: List<BluetoothDevice>) : ScanningState
    data class Error(val error: String) : ScanningState
}