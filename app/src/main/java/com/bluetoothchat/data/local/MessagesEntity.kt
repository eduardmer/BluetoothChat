package com.bluetoothchat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessagesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val local_device: String,
    val remote_device: String,
    val message: String,
    val isFromLocal: Boolean,
    val date: Long
)
