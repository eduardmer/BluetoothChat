package com.bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toDomainModel() = com.bluetoothchat.model.BluetoothDevice(
    name.orEmpty(),
    address.orEmpty()
)