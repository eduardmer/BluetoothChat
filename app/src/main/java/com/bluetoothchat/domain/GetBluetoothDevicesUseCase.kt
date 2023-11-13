package com.bluetoothchat.domain

import com.bluetoothchat.model.ConnectionState
import com.bluetoothchat.model.ScanningState
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBluetoothDevicesUseCase @Inject constructor(
    private val bluetoothController: BluetoothController
) {

    operator fun invoke() = combine(bluetoothController.scanningState, bluetoothController.connectionState)
    { scanningState, connectionState ->
        BluetoothUiState(
            when (scanningState) {
                ScanningState.EmptyValue -> emptyList()
                ScanningState.ScanningStarted -> emptyList()
                is ScanningState.ScanningInProgress -> scanningState.devices
                is ScanningState.ScanningFinished -> scanningState.devices
            },
            scanningState is ScanningState.ScanningInProgress,
            connectionState is ConnectionState.ConnectionAccepted,
            connectionState is ConnectionState.ConnectionInitiated,
            connectionState is ConnectionState.ConnectionInitiated,
            if (connectionState is ConnectionState.ConnectionError)
                connectionState.error.message ?: "Error"
            else
                    null
        )
    }

}