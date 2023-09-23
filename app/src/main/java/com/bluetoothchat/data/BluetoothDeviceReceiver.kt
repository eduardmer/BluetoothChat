package com.bluetoothchat.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothDeviceReceiver(
    private val onDiscoveryStarted: () -> Unit,
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onDiscoveryFinished: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> onDiscoveryStarted()
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let { onDeviceFound(it) }
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onDiscoveryFinished()
        }
    }

}