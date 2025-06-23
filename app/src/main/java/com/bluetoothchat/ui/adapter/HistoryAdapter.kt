package com.bluetoothchat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.databinding.ViewUserBinding

class HistoryAdapter(
    private val onClickListener: (String) -> Unit
) : ListAdapter<MessagesEntity, HistoryAdapter.MyViewHolder>(DiffCallBack()) {

    class MyViewHolder(val binding: ViewUserBinding, val onClickListener: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MessagesEntity) {
            binding.item = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                onClickListener(item.remote_device)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ViewUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, onClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallBack : DiffUtil.ItemCallback<MessagesEntity>() {
        override fun areItemsTheSame(oldItem: MessagesEntity, newItem: MessagesEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessagesEntity, newItem: MessagesEntity): Boolean {
            return oldItem == newItem
        }
    }

}