package com.bluetoothchat.ui.view_holder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.databinding.ViewRemoteUserMessageBinding

class RemoteUserMessageViewHolder(private val binding: ViewRemoteUserMessageBinding) : ViewHolder(binding.root) {

    fun bind(item: MessagesEntity) {
        binding.item = item
        binding.executePendingBindings()
    }

}