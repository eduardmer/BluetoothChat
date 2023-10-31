package com.bluetoothchat.ui.binding

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.bluetoothchat.R

@BindingAdapter("state")
fun Button.changeState(state: String) {
    tag = state
    text = if (state == "stop") {
        setBackgroundColor(resources.getColor(R.color.red))
        resources.getString(R.string.stop_scan)
    } else {
        setBackgroundColor(resources.getColor(R.color.blue))
        resources.getString(R.string.start_scan)
    }
}