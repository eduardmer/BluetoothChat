package com.bluetoothchat.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.bluetoothchat.R

fun Context.showWarningDialog(titleId: Int = R.string.warning, message: String) {
    AlertDialog.Builder(this)
        .setTitle(titleId)
        .setMessage(message)
        .setIcon(R.drawable.ic_warning)
        .setPositiveButton(R.string.done) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}

fun Context.showErrorDialog(titleId: Int = R.string.error, message: String) =
    AlertDialog.Builder(this)
        .setTitle(titleId)
        .setMessage(message)
        .setIcon(R.drawable.ic_error)
        .setPositiveButton(R.string.done) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()