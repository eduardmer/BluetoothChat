package com.bluetoothchat.ui.binding

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.bluetoothchat.R

@BindingAdapter("state")
fun Button.changeButtonState(isScanning: Boolean) {
    text = if (isScanning) {
        tag = "stop"
        setBackgroundColor(resources.getColor(R.color.red))
        resources.getString(R.string.stop_scan)
    } else {
        tag = "start"
        setBackgroundColor(resources.getColor(R.color.blue))
        resources.getString(R.string.start_scan)
    }
}