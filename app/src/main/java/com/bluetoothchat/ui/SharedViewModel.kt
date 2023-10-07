package com.bluetoothchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.GetBluetoothDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
    getBluetoothDevicesUseCase: GetBluetoothDevicesUseCase
) : ViewModel() {

    val state = getBluetoothDevicesUseCase()

    val serverState = bluetoothController.connectionState

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

    override fun onCleared() {
        super.onCleared()
        bluetoothController.stopDiscovery()
    }

}