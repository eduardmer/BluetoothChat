package com.bluetoothchat.utils

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bluetoothchat.R
import com.bluetoothchat.model.Permission

class PermissionManager(private val fragment: Fragment) {

    private val permissions = mutableListOf<String>()
    private var callBack: (Boolean) -> Unit = {}
    private var permissionRationale: String = ""
    private val permissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        sendResult(perms.all { it.value })
    }

    /**
     * Add the permission you want to check if it is granted or not
     * @param permission the permission you want to check
     */
    fun requestPermission(permission: Permission): PermissionManager {
        permissions.addAll(permission.permissions)
        return this
    }

    /**
     * Check if the specified permissions are granted or not
     * @param callBack the code that will be invoked after checking permissions
     */
    fun checkPermission(callBack: (Boolean) -> Unit) {
        this.callBack = callBack
        handlePermissionResult()
    }

    private fun handlePermissionResult() {
        when {
            areAllPermissionsGranted() -> sendResult(true)
            shouldShowRequestPermissionsRationale() -> {
                fragment.requireContext().showWarningDialog(
                    message = fragment.requireContext().getString(R.string.request_permission, permissionRationale)
                )
                sendResult(false)
            }
            else -> permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun sendResult(result: Boolean) {
        callBack(result)
        permissions.clear()
        callBack = {}
        permissionRationale = ""
    }

    private fun areAllPermissionsGranted(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(fragment.requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRequestPermissionsRationale(): Boolean {
        val perm = permissions.find { fragment.shouldShowRequestPermissionRationale(it) }
        return if (perm != null) {
            permissionRationale = perm
            true
        } else false
    }

}