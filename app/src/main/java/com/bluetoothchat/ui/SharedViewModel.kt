package com.bluetoothchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.GetBluetoothDevicesUseCase
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    getBluetoothDevicesUseCase: GetBluetoothDevicesUseCase
) : ViewModel() {

    val serverState = bluetoothController.connectionState

    val connectionState = bluetoothController.isDeviceConnected.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ConnectionResult.Disconnected
    )

    fun startDiscovery(): Flow<DiscoveryResult> {
        return bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    fun openServer(): Flow<ConnectionResult> {
        return bluetoothController.openServer()
    }

    fun stopServer() {
        bluetoothController.stopServer()
    }

    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothController.connectToDevice(device)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.stopDiscovery()
    }

}