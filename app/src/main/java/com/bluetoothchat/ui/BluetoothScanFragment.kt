package com.bluetoothchat.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.R
import com.bluetoothchat.databinding.FragmentBluetoothScanBinding
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import com.bluetoothchat.ui.adapter.BluetoothDevicesAdapter
import com.bluetoothchat.ui.binding.changeState
import com.bluetoothchat.utils.addMenu
import com.bluetoothchat.utils.launchAndRepeatWithViewLifecycle
import com.bluetoothchat.utils.showErrorDialog
import com.bluetoothchat.utils.showProgressDialog
import com.bluetoothchat.utils.showWarningDialog
import com.permission_manager.PermissionManager
import com.permission_manager.PermissionResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job

@AndroidEntryPoint
class BluetoothScanFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentBluetoothScanBinding
    private lateinit var bluetoothDevicesAdapter: BluetoothDevicesAdapter
    private val permissionManager = PermissionManager(this)
    private var job: Job? = null
    private var bluetoothServerJob: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBluetoothScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addMenu(R.menu.scan_menu) {
            if (it.itemId == R.id.open_server) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                    permissionManager.requestPermission(Manifest.permission.BLUETOOTH_CONNECT)
                        .checkPermissions { result ->
                            when (result) {
                                PermissionResult.PERMISSIONS_GRANTED -> openBluetoothServer()
                                PermissionResult.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(message = R.string.request_permission)
                                else -> {}
                            }
                        }
                else
                    openBluetoothServer()
                true
            }
            else false
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.connectionState.collect { result ->
                when(result) {
                    is ConnectionResult.ConnectionAccepted -> findNavController().navigate(R.id.action_bluetoothScanFragment_to_chatFragment)
                    else -> {}
                }
            }
        }
        bluetoothDevicesAdapter = BluetoothDevicesAdapter {
            viewModel.connectToDevice(it)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = bluetoothDevicesAdapter
        }
        binding.scanButton.setOnClickListener {
            if (binding.scanButton.tag == "start") {
                permissionManager.requestPermission(
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                        Manifest.permission.BLUETOOTH_SCAN
                    else {
                        Manifest.permission.ACCESS_FINE_LOCATION
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    }
                ).checkPermissions { result ->
                    when (result) {
                        PermissionResult.PERMISSIONS_GRANTED -> scanBluetoothDevices()
                        PermissionResult.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(message = R.string.request_permission)
                        else -> {}
                    }
                }
            } else
                viewModel.stopDiscovery()
        }

    }

    private fun scanBluetoothDevices() {
        job?.cancel()
        job = launchAndRepeatWithViewLifecycle {
            viewModel.startDiscovery().collect { result ->
                when(result) {
                    DiscoveryResult.DiscoveryStarted -> {
                        binding.scanButton.changeState("stop")
                        binding.textView2.isVisible = false
                        binding.textView3.isVisible = false
                        binding.recyclerView.isVisible = true
                    }
                    is DiscoveryResult.DiscoveryInProgress -> bluetoothDevicesAdapter.submitList(result.devices)
                    is DiscoveryResult.DiscoveryFinished -> {
                        binding.scanButton.changeState("start")
                        binding.emptyItem.root.isVisible = result.devices.isEmpty()
                        bluetoothDevicesAdapter.submitList(result.devices)
                    }
                    is DiscoveryResult.DiscoveryError -> requireContext().showErrorDialog(message = getString(result.error.messageId))
                }
            }
        }
    }

    private fun openBluetoothServer() {
        bluetoothServerJob = launchAndRepeatWithViewLifecycle {
            viewModel.openServer().collect { result ->
                when (result) {
                    ConnectionResult.ConnectionInitiated -> requireContext().showProgressDialog(
                        R.string.waiting_for_connection,
                        R.string.close_server,
                        R.drawable.ic_bluetooth_connect
                    ) { dialog ->
                        bluetoothServerJob?.cancel()
                        viewModel.stopServer()
                        dialog.dismiss()
                    }
                    is ConnectionResult.ConnectionError -> requireContext().showErrorDialog(
                        message = result.error.message ?: "Error"
                    )

                    is ConnectionResult.ConnectionAccepted -> findNavController().navigate(R.id.action_bluetoothScanFragment_to_chatFragment)
                    ConnectionResult.Disconnected -> Toast.makeText(
                        requireContext(),
                        "Disconnected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}