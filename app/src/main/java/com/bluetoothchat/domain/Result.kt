package com.bluetoothchat.domain

import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult

sealed interface Result {
    data class Success(val discoveryState: DiscoveryResult, val connectionState: ConnectionResult) : Result
    data class Error(val error: Throwable) : Result
}