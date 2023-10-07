package com.bluetoothchat.ui

import android.Manifest
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.R
import com.bluetoothchat.databinding.FragmentBluetoothScanBinding
import com.bluetoothchat.model.DiscoveryResult
import com.bluetoothchat.domain.Result
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.ui.adapter.BluetoothDevicesAdapter
import com.bluetoothchat.utils.addMenu
import com.bluetoothchat.utils.showErrorDialog
import com.bluetoothchat.utils.showWarningDialog
import com.permission_manager.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BluetoothScanFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentBluetoothScanBinding
    private lateinit var bluetoothDevicesAdapter: BluetoothDevicesAdapter
    private val permissionManager = PermissionManager(this)
    private var progressDialog: ProgressDialog? = null

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
                                com.permission_manager.Result.PERMISSIONS_GRANTED -> viewModel.openServer()
                                com.permission_manager.Result.PERMISSIONS_DENIED -> Log.i("PermissionConnectResult", "Permissions denied")
                                com.permission_manager.Result.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(message = "Permission Bluetooth Connect")
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect { result ->
                    when(result) {
                        is Result.Success -> handleBluetoothScanning(result.discoveryState)
                        is Result.Error ->
                            Toast.makeText(requireContext(), result.error.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.serverState.collect { result ->
                    when(result) {
                        ConnectionResult.ConnectionInitiated -> showProgressBar(true)
                        is ConnectionResult.ConnectionAccepted -> {
                            showProgressBar(false)
                            findNavController().navigate(R.id.action_bluetoothScanFragment_to_chatFragment)
                        }
                        is ConnectionResult.ConnectionError -> {
                            showProgressBar(false)
                            requireContext().showErrorDialog(message = result.error.message ?: "Error")
                        }
                        ConnectionResult.Disconnected -> Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.scanButton.setOnClickListener {
            permissionManager.requestPermission(
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                    Manifest.permission.BLUETOOTH_SCAN
                else {
                    Manifest.permission.ACCESS_FINE_LOCATION
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }
            ).checkPermissions { result ->
                when(result) {
                    com.permission_manager.Result.PERMISSIONS_GRANTED -> viewModel.startDiscovery()
                    com.permission_manager.Result.PERMISSIONS_DENIED -> Log.i("PermissionsResult", "Permissions denied")
                    com.permission_manager.Result.PERMISSIONS_RATIONALE -> requireContext().showWarningDialog(message = "Permission")
                }
            }
        }
    }

    private fun handleBluetoothScanning(result: DiscoveryResult) {
        when(result) {
            DiscoveryResult.DiscoveryStarted -> enableScanButton(false)
            is DiscoveryResult.DiscoveryFinished -> {
                enableScanButton(true)
                binding.textView2.isVisible = false
                binding.textView3.isVisible = false
                binding.recyclerView.isVisible = true
                binding.emptyItem.root.isVisible = result.devices.isEmpty()
                bluetoothDevicesAdapter.submitList(result.devices)
            }
            is DiscoveryResult.DiscoveryError ->
                requireContext().showWarningDialog(message = getString(result.error.messageId))
        }
    }

    private fun enableScanButton(shouldEnable: Boolean) {
        binding.scanButton.isEnabled = shouldEnable
    }

    private fun showProgressBar(isVisible: Boolean) {
        if (!isVisible)
            progressDialog?.dismiss()
        else
            progressDialog = ProgressDialog(requireContext()).apply {
                setTitle("Server is open")
                setMessage("Waiting for connection")
                setCancelable(false)
                setButton("Stop server") { dialog, _ ->
                    viewModel.stopServer()
                    dialog.dismiss()
                }
                show()
            }
    }

}