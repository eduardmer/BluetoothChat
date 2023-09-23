package com.bluetoothchat.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.R
import com.bluetoothchat.databinding.FragmentScannedDevicesBinding
import com.bluetoothchat.model.ConnectionResult
import com.bluetoothchat.model.DiscoveryResult
import com.bluetoothchat.domain.Result
import com.bluetoothchat.ui.adapter.BluetoothDevicesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScannedDevicesFragment : Fragment() {

    private val viewModel: SharedViewModel by viewModels()
    private lateinit var binding: FragmentScannedDevicesBinding
    private lateinit var bluetoothDevicesAdapter: BluetoothDevicesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScannedDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        is Result.Success -> {
                            handleBluetoothScanning(result.discoveryState)
                            when(result.connectionState) {
                                is ConnectionResult.ConnectionAccepted -> {
                                    viewModel.stopDiscovery()
                                    findNavController().navigate(R.id.chatFragment)
                                }
                                is ConnectionResult.ConnectionError -> Toast.makeText(requireContext(), result.connectionState.error.message ?: "Error", Toast.LENGTH_SHORT).show()
                                ConnectionResult.ConnectionInitiated -> Toast.makeText(requireContext(), "Connection initiated", Toast.LENGTH_SHORT).show()
                                ConnectionResult.ConnectionNotInitiated -> Toast.makeText(requireContext(), "Connection NotInitiated", Toast.LENGTH_SHORT).show()
                                ConnectionResult.Disconnected -> Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is Result.Error -> {
                            Toast.makeText(requireContext(), result.error.message ?: "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun handleBluetoothScanning(result: DiscoveryResult) {

    }

}