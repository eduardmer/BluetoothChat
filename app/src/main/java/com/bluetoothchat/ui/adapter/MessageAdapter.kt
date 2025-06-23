package com.bluetoothchat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.databinding.ViewLocalUserMessageBinding
import com.bluetoothchat.databinding.ViewRemoteUserMessageBinding
import com.bluetoothchat.ui.view_holder.LocalUserMessageViewHolder
import com.bluetoothchat.ui.view_holder.RemoteUserMessageViewHolder

class MessageAdapter : ListAdapter<MessagesEntity, ViewHolder>(DiffCallBack()) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isFromLocal) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val binding =
                ViewLocalUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LocalUserMessageViewHolder(binding)
        }
        val binding =
            ViewRemoteUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RemoteUserMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is LocalUserMessageViewHolder)
            holder.bind(getItem(position))
        else if (holder is RemoteUserMessageViewHolder)
            holder.bind(getItem(position))
    }

    class DiffCallBack : DiffUtil.ItemCallback<MessagesEntity>() {
        override fun areItemsTheSame(
            oldItem: MessagesEntity,
            newItem: MessagesEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MessagesEntity,
            newItem: MessagesEntity
        ): Boolean {
            return oldItem == newItem
        }
    }

}