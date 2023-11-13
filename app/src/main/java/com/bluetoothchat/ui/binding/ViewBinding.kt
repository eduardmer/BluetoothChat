package com.bluetoothchat.ui

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.bluetoothchat.R
import com.bluetoothchat.model.ScanningState

@BindingAdapter("state")
fun Button.changeButtonState(state: ScanningState) {
    text = if (state is ScanningState.ScanningStarted || state is ScanningState.ScanningInProgress) {
        tag = "stop"
        setBackgroundColor(resources.getColor(R.color.red))
        resources.getString(R.string.stop_scan)
    } else {
        tag = "start"
        setBackgroundColor(resources.getColor(R.color.blue))
        resources.getString(R.string.start_scan)
    }
}