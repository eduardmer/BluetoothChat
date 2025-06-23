package com.bluetoothchat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.ChatRepository
import com.bluetoothchat.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    chatRepository: ChatRepository
) : ViewModel() {

    val latestMessages = chatRepository.getLastMessageForEachDevice()

    private val _state = MutableStateFlow(UiState(connectionResult = ConnectionState.Disconnected))
    val state = combine(
        bluetoothController.scanningState,
        bluetoothController.connectionState,
        _state
    ) { scanningState, connectionState, uiState ->
        uiState.copy(
            scannedDevices = scanningState.scannedDevices,
            hasScannedBefore = scanningState.hasScannedBefore,
            isScanning = scanningState.isScanning,
            error = scanningState.error,
            connectionResult = connectionState
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _state.value
    )

    fun startDiscovery() {
        bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    fun openServer() {
        viewModelScope.launch {
            bluetoothController.openServer()
        }
    }

    fun closeConnection() {
        viewModelScope.launch {
            bluetoothController.closeConnection()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothController.connectToDevice(device)
        }
    }

    fun deleteError() {
        bluetoothController.deleteError()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.stopDiscovery()
    }

}

data class UiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val hasScannedBefore: Boolean = false,
    val isScanning: Boolean = false,
    val error: String? = null,
    val connectionResult: ConnectionState
)