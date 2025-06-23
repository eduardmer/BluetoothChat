package com.bluetoothchat.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.R
import com.bluetoothchat.databinding.FragmentBluetoothScanBinding
import com.bluetoothchat.model.ConnectionState
import com.bluetoothchat.ui.adapter.BluetoothDevicesAdapter
import com.bluetoothchat.utils.addMenu
import com.bluetoothchat.utils.launchAndRepeatWithViewLifecycle
import com.bluetoothchat.utils.showErrorDialog
import com.bluetoothchat.utils.showProgressDialog
import com.bluetoothchat.utils.showWarningDialog
import com.permission_manager.PermissionManager
import com.permission_manager.PermissionResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BluetoothScanFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentBluetoothScanBinding
    private lateinit var bluetoothDevicesAdapter: BluetoothDevicesAdapter
    private val permissionManager = PermissionManager(this)
    private var progressDialog: AlertDialog? = null

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
                                PermissionResult.PERMISSIONS_GRANTED -> viewModel.openServer()
                                PermissionResult.PERMISSIONS_DENIED -> Log.i("PermissionConnectResult", "Permissions denied")
                                PermissionResult.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(message = "Permission Bluetooth Connect")
                            }
                        }
                else
                    viewModel.openServer()
                true
            }
            else false
        }
        bluetoothDevicesAdapter = BluetoothDevicesAdapter {
            viewModel.connectToDevice(it)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = bluetoothDevicesAdapter
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.state.collect {
                handleScanning(it)
                handleDeviceConnection(it.connectionResult)
            }
        }
        binding.scanButton.setOnClickListener {
            if (binding.scanButton.tag == "start") {
                permissionManager.requestPermission(
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                        Manifest.permission.BLUETOOTH_SCAN
                    else
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).checkPermissions { result ->
                    when (result) {
                        PermissionResult.PERMISSIONS_GRANTED -> viewModel.startDiscovery()
                        PermissionResult.PERMISSIONS_DENIED -> Log.i(
                            "PermissionsResult",
                            "Permissions denied"
                        )
                        PermissionResult.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(
                            message = "Permission"
                        )
                    }
                }
            }
            else
                viewModel.stopDiscovery()
        }
    }

    private fun handleScanning(result: UiState) {
        binding.state = result
        if (result.hasScannedBefore)
            bluetoothDevicesAdapter.submitList(result.scannedDevices)
        if (result.error != null)
            requireContext().showErrorDialog(
                message = result.error,
                onClickListener = {
                    it.dismiss()
                },
                onDismissListener = {
                    viewModel.deleteError()
                }
            )
    }

    private fun handleDeviceConnection(result: ConnectionState) {
        when(result) {
            is ConnectionState.ConnectionAccepted -> {
                progressDialog?.dismiss()
                findNavController().navigate(BluetoothScanFragmentDirections.actionBluetoothScanFragmentToChatFragment(result.device.address))
            }
            is ConnectionState.ConnectionError -> {
                requireContext().showErrorDialog(
                    message = result.error.message ?: getString(R.string.error),
                    onClickListener = {
                        it.dismiss()
                        viewModel.closeConnection()
                    }
                )
            }
            ConnectionState.ConnectionInitiated -> {
                progressDialog = requireContext().showProgressDialog(
                    R.string.waiting_for_connection,
                    R.string.close_server,
                    R.drawable.ic_bluetooth_connect
                ) { dialog ->
                    viewModel.closeConnection()
                    dialog.dismiss()
                }
                progressDialog?.show()
            }
            ConnectionState.Disconnected -> {}
        }
    }

}