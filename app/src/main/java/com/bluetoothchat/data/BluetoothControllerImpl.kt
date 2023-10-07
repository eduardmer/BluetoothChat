package com.bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryError
import com.bluetoothchat.model.DiscoveryResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _discoveryState = MutableSharedFlow<DiscoveryResult>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    private val _connectionState = MutableSharedFlow<ConnectionResult>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )
    override val discoveryState: SharedFlow<DiscoveryResult>
        get() = _discoveryState.asSharedFlow()
    override val connectionState: SharedFlow<ConnectionResult>
        get() = _connectionState.asSharedFlow()

    private val allDevices = mutableListOf<BluetoothDevice>()
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    private val foundDeviceReceiver = BluetoothDeviceReceiver(
        {
            allDevices.clear()
            bluetoothAdapter?.bondedDevices?.map {
                it.toDomainModel()
            }?.also {
                allDevices.addAll(it)
            }
            _discoveryState.tryEmit(DiscoveryResult.DiscoveryStarted)
        }, { device ->
            val newDevice = device.toDomainModel()
            if (!allDevices.contains(newDevice))
                allDevices.add(newDevice)
        }, {
            _discoveryState.tryEmit(DiscoveryResult.DiscoveryFinished(allDevices))
        }
    )

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, device ->
        if (isConnected && device != null)
            _connectionState.tryEmit(ConnectionResult.ConnectionAccepted(device.toDomainModel()))
        else
            _connectionState.tryEmit(ConnectionResult.Disconnected)
    }

    init {
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R && bluetoothAdapter?.isEnabled == false)
            _discoveryState.tryEmit(DiscoveryResult.DiscoveryError(DiscoveryError.BLUETOOTH_NOT_ENABLED))
        else if (bluetoothAdapter?.isEnabled == false || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            _discoveryState.tryEmit(DiscoveryResult.DiscoveryError(DiscoveryError.BLUETOOTH_LOCATION_NOT_ENABLED))
        else if (bluetoothAdapter?.isDiscovering != true) {
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
    }

    override fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override suspend fun openServer() = withContext(Dispatchers.IO) {
        if (bluetoothAdapter?.isEnabled == false) {
            _connectionState.emit(ConnectionResult.ConnectionError(Throwable("Bluetooth is off")))
            return@withContext
        }
        _connectionState.emit(ConnectionResult.ConnectionInitiated)
        serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
            "BluetoothChat",
            UUID.fromString(SERVICE_UUID)
        )
        var shouldLoop = true
        while (shouldLoop) {
            clientSocket = try {
                serverSocket?.accept()
            } catch (error: Exception) {
                _connectionState.emit(ConnectionResult.ConnectionError(error))
                shouldLoop = false
                null
            }
            clientSocket?.also {
                closeServerSocket()
                shouldLoop = false
            }
        }
    }

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