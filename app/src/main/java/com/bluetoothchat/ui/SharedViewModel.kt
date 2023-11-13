package com.bluetoothchat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.BluetoothMessage
import com.bluetoothchat.domain.GetBluetoothDevicesUseCase
import com.bluetoothchat.model.ConnectionState
import com.bluetoothchat.model.ScanningState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    getBluetoothDevicesUseCase: GetBluetoothDevicesUseCase
) : ViewModel() {

    val state = combine(
        bluetoothController.scanningState,
        bluetoothController.connectionState
    ) { scanningState: ScanningState, connectionState: ConnectionState ->
        UiState(scanningState, connectionState)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UiState(ScanningState.EmptyValue, ConnectionState.Disconnected)
    )

    val chatState = bluetoothController.dataTransferService?.listenForMessages()?.catch {
        Log.i("error message", it.message ?: "error")
    }

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

    fun stopServer() {
        viewModelScope.launch {
            bluetoothController.stopServer()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothController.connectToDevice(device)
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            bluetoothController.sendMessage(message)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.stopDiscovery()
    }

}

data class UiState(
    val scannedResult: ScanningState,
    val connectionResult: ConnectionState
)