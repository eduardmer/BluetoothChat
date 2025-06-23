package com.bluetoothchat.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluetoothchat.model.BluetoothDevice
import com.bluetoothchat.databinding.BluetoothDeviceItemBinding

class BluetoothDevicesAdapter(
    private val onItemClicked: (BluetoothDevice) -> Unit
) : ListAdapter<BluetoothDevice, BluetoothDevicesAdapter.BluetoothDeviceViewHolder>(DiffCallBack()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        val binding = BluetoothDeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BluetoothDeviceViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallBack : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.name == newItem.name
        }
    }

}