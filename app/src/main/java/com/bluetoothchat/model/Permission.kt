package com.bluetoothchat.model

import android.Manifest

sealed class Permission(vararg val permissions: String) {

    object BLUETOOTH_SCAN : Permission(Manifest.permission.BLUETOOTH_SCAN)

    object BLUETOOTH_CONNECT : Permission(Manifest.permission.BLUETOOTH_CONNECT)

    object LOCATION : Permission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun getName(): String {
        return when(this) {
            BLUETOOTH_SCAN -> "Scan"
            BLUETOOTH_CONNECT -> "Connect"
            LOCATION -> "Location"
        }
    }

}
