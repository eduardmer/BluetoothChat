package com.bluetoothchat.model

import com.bluetoothchat.R

enum class DiscoveryError(val messageId: Int) {
    BLUETOOTH_NOT_ENABLED(R.string.enable_bluetooth),
    BLUETOOTH_LOCATION_NOT_ENABLED(R.string.enable_bluetooth_location),
    //PERMISSIONS_NOT_GRANTED(R.string.request_permission)
}