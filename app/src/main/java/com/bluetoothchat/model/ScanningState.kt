package com.bluetoothchat.model

sealed interface ScanningState {
    object ScanningStarted : ScanningState
    data class ScanningInProgress(val devices: List<BluetoothDevice>) : ScanningState
    data class ScanningFinished(val devices: List<BluetoothDevice>) : ScanningState
    object EmptyValue : ScanningState
}