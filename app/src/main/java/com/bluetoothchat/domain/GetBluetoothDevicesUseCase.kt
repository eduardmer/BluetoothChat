package com.bluetoothchat.domain

import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class GetBluetoothDevicesUseCase @Inject constructor(private val bluetoothController: BluetoothController) {

    operator fun invoke() = bluetoothController.discoveryState.map<DiscoveryResult, Result> { discoveryState ->
        Result.Success(discoveryState, ConnectionResult.ConnectionNotInitiated)
    }.catch {
        emit(Result.Error(it))
    }

}