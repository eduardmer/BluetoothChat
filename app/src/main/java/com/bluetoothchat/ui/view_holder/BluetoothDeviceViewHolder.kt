package com.bluetoothchat.ui.view_holder

import androidx.recyclerview.widget.RecyclerView
import com.bluetoothchat.databinding.BluetoothDeviceItemBinding
import com.bluetoothchat.model.BluetoothDevice

class BluetoothDeviceViewHolder(
    private val binding: BluetoothDeviceItemBinding,
    private val onItemClicked: (BluetoothDevice) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BluetoothDevice) {
        binding.item = item
        binding.root.setOnClickListener {
            onItemClicked(item)
        }
        binding.executePendingBindings()
    }

}