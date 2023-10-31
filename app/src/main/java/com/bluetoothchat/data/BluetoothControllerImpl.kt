package com.bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryError
import com.bluetoothchat.model.DiscoveryResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class BluetoothControllerImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val locationManager: LocationManager
) : BluetoothController {

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }

    private val _connectionState = MutableSharedFlow<ConnectionResult>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    override val connectionState: SharedFlow<ConnectionResult>
        get() = _connectionState.asSharedFlow()

    private val allDevices = mutableListOf<BluetoothDevice>()
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    override val isDeviceConnected = callbackFlow {
        val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, device ->
            if (isConnected && device != null)
                trySend(ConnectionResult.ConnectionAccepted(device.toDomainModel()))
            else
                trySend(ConnectionResult.Disconnected)
        }

        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )

        awaitClose {
            context.unregisterReceiver(bluetoothStateReceiver)
        }
    }

    override fun startDiscovery() = callbackFlow {
        val foundDeviceReceiver = BluetoothDeviceReceiver(
            {
                allDevices.clear()
                bluetoothAdapter?.bondedDevices?.map {
                    it.toDomainModel()
                }?.also {
                    allDevices.addAll(it)
                }
                trySend(DiscoveryResult.DiscoveryStarted)
            }, { device ->
                val newDevice = device.toDomainModel()
                if (!allDevices.contains(newDevice))
                    allDevices.add(newDevice)
                trySend(DiscoveryResult.DiscoveryInProgress(allDevices))
            }, {
                trySend(DiscoveryResult.DiscoveryFinished(allDevices))
            }
        )
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R && bluetoothAdapter?.isEnabled == false)
            trySend(DiscoveryResult.DiscoveryError(DiscoveryError.BLUETOOTH_NOT_ENABLED))
        else if (bluetoothAdapter?.isEnabled == false || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            trySend(DiscoveryResult.DiscoveryError(DiscoveryError.BLUETOOTH_LOCATION_NOT_ENABLED))
        else {
            context.registerReceiver(
                foundDeviceReceiver,
                IntentFilter().apply {
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                    addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
            )
            bluetoothAdapter?.startDiscovery()
        }

        awaitClose {
            bluetoothAdapter?.cancelDiscovery()
        }

    }.onCompletion {
        Log.i("BluetoothScanner", "scaning onCompletion")
    }

    override fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun openServer() = flow {
        emit(ConnectionResult.ConnectionInitiated)
        serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
            "BluetoothChat",
            UUID.fromString(SERVICE_UUID)
        )
        var shouldLoop = true
        while (shouldLoop) {
            clientSocket = try {
                serverSocket?.accept()
            } catch (error: Exception) {
                emit(ConnectionResult.ConnectionError(error))
                shouldLoop = false
                null
            }
            clientSocket?.also {
                closeServerSocket()
                shouldLoop = false
            }
        }
    }.onCompletion {
        closeServerSocket()
    }.flowOn(Dispatchers.IO)

    override suspend fun connectToDevice(device: BluetoothDevice): Unit = withContext(Dispatchers.IO) {
        stopDiscovery()
        clientSocket = bluetoothAdapter
            ?.getRemoteDevice(device.address)
            ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))
        clientSocket?.let {
            try {
                it.connect()
            } catch (error: Exception) {
                it.close()
                clientSocket = null
                _connectionState.tryEmit(ConnectionResult.ConnectionError(error))
            }
        }
    }

    override fun stopServer() {
        serverSocket?.close()
        clientSocket?.close()
        serverSocket = null
        clientSocket = null
    }

    /**
     * the server socket should be closed to not accept more connections
     */
    private fun closeServerSocket() {
        serverSocket?.close()
        serverSocket = null
    }

}