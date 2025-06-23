package com.bluetoothchat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.databinding.FragmentHomeBinding
import com.bluetoothchat.ui.adapter.HistoryAdapter
import com.bluetoothchat.utils.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesHistoryFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: SharedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = HistoryAdapter { deviceAddress ->
            findNavController().navigate(MessagesHistoryFragmentDirections.actionHomeFragmentToChatFragment(deviceAddress))
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        launchAndRepeatWithViewLifecycle {
            viewModel.latestMessages.collect {
                adapter.submitList(it)
                val isListEmpty = it.isEmpty()
                binding.apply {
                    recyclerView.isVisible = !isListEmpty
                    messageImage.isVisible = isListEmpty
                    noMessagesTitle.isVisible = isListEmpty
                    noMessagesDesc.isVisible = isListEmpty
                }
            }
        }
    }

}