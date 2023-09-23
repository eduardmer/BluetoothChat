package com.bluetoothchat.domain

data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean
)

fun String.toBluetoothMessage(isFromLocalUser: Boolean) = BluetoothMessage(
    this,
    "sender",
    isFromLocalUser
)
