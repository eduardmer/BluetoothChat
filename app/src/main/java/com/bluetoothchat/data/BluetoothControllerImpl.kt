package com.bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.model.ConnectionState
import com.bluetoothchat.model.ScanningState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    override var dataTransferService: BluetoothDataTransfer? = null
    private val _scanningState = MutableStateFlow<ScanningState>(ScanningState.EmptyValue)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val scanningState: StateFlow<ScanningState>
        get() = _scanningState.asStateFlow()
    override val connectionState: StateFlow<ConnectionState>
        get() = _connectionState.asStateFlow()

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
            _scanningState.update {
                ScanningState.ScanningStarted
            }
        }, { device ->
            val newDevice = device.toDomainModel()
            if (!allDevices.contains(newDevice))
                allDevices.add(newDevice)
            _scanningState.update {
                ScanningState.ScanningInProgress(allDevices)
            }
        }, {
            _scanningState.update {
                ScanningState.ScanningFinished(allDevices)
            }
        }
    )

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, device ->
        if (isConnected && device != null)
            _connectionState.update {
                ConnectionState.ConnectionAccepted(device.toDomainModel())
            }
        else
            _connectionState.update {
                ConnectionState.Disconnected
            }
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

    override fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    override suspend fun openServer() = withContext(Dispatchers.IO) {
        _connectionState.update {
            ConnectionState.ConnectionInitiated
        }
        serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
            "BluetoothChat",
            UUID.fromString(SERVICE_UUID)
        )
        var shouldLoop = true
        while (shouldLoop) {
            clientSocket = try {
                serverSocket?.accept()
            } catch (error: Exception) {
                _connectionState.update {
                    ConnectionState.Disconnected
                }
                shouldLoop = false
                null
            }
            clientSocket?.also {
                closeServerSocket()
                shouldLoop = false
                dataTransferService = BluetoothDataTransfer(it)
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
                dataTransferService = BluetoothDataTransfer(it)
            } catch (error: Exception) {
                it.close()
                clientSocket = null
                _connectionState.update {
                    ConnectionState.ConnectionError(error)
                }
            }
        }
    }

    override fun stopServer() {
        serverSocket?.close()
        clientSocket?.close()
        serverSocket = null
        clientSocket = null
        _connectionState.update {
            ConnectionState.Disconnected
        }
    }

    override suspend fun sendMessage(message: String) {
        /*val message = BluetoothMessage(
            message,
            bluetoothAdapter?.name ?: "NAME",
            true
        )*/
        dataTransferService?.sendMessage(message.encodeToByteArray())
    }

    /**
     * the server socket should be closed to not accept more connections
     */
    private fun closeServerSocket() {
        serverSocket?.close()
        serverSocket = null
    }

}