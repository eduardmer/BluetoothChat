package com.bluetoothchat.utils

import android.content.Context
import android.content.DialogInterface
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

fun Context.showErrorDialog(
    titleId: Int = R.string.error,
    message: String,
    onClickListener: (DialogInterface) -> Unit,
    onDismissListener: () -> Unit = {}
) =
    AlertDialog.Builder(this)
        .setTitle(titleId)
        .setMessage(message)
        .setIcon(R.drawable.ic_error)
        .setPositiveButton(R.string.done) { dialog, _ ->
            onClickListener(dialog)
        }.setOnDismissListener {
            onDismissListener()
        }
        .create()
        .show()

fun Context.showProgressDialog(titleId: Int, buttonText: Int, iconId: Int, onClickListener: (DialogInterface) -> Unit): AlertDialog =
    AlertDialog.Builder(this)
        .setTitle(titleId)
        .setIcon(iconId)
        .setView(R.layout.view_progress)
        .setNegativeButton(buttonText) { dialog, _ ->
            onClickListener(dialog)
        }
        .setCancelable(false)
        .create()
