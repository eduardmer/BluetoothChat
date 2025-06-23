package com.bluetoothchat.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluetoothchat.R
import com.bluetoothchat.databinding.FragmentChatBinding
import com.bluetoothchat.ui.adapter.MessageAdapter
import com.bluetoothchat.utils.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat) {

    private var binding: FragmentChatBinding? = null
    private val viewModel: ChatViewModel by viewModels()
    private val msgAdapter = MessageAdapter()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatBinding.bind(view)
        binding?.sendButton?.setOnClickListener {
            viewModel.sendMessage(binding?.messageText?.text.toString())
//            launchAndRepeatWithViewLifecycle {
//                viewModel.sendMessagee(binding?.messageText?.text.toString())?.collect {
//                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
//                }
//            }
        }
        binding?.recyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        binding?.recyclerView?.adapter = msgAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding?.sendButton?.isEnabled = state.isConnected
                    msgAdapter.submitList(state.messages.reversed())
                    binding?.recyclerView?.smoothScrollToPosition(0)
                    if (state.error != null)
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}