package com.permission_manager

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionManager(private val fragment: Fragment) {

    private val permissions = mutableListOf<String>()
    private var block: (PermissionResult) -> Unit = {}
    private val permissionsRationale = mutableListOf<String>()

    private val permissionCheck =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            sendResult(
                if (result.all { it.value })
                    PermissionResult.PERMISSIONS_GRANTED
                else PermissionResult.PERMISSIONS_DENIED
            )
        }

    /**
     * Add the permissions you want to check if they are granted or not
     * @param permissions the permissions you want to check
     */
    fun requestPermission(vararg permissions: String): PermissionManager {
        this.permissions.addAll(permissions)
        return this
    }

    /**
     * Check if the specified permissions are granted or not
     * @param block the code that will be invoked after checking permissions
     */
    fun checkPermissions(block: (PermissionResult) -> Unit) {
        this.block = block
        handlePermissionRequest()
    }

    private fun handlePermissionRequest() {
        when {
            areAllPermissionsGranted() -> sendResult(PermissionResult.PERMISSIONS_GRANTED)
            shouldShowRequestPermissionRationale() -> sendResult(PermissionResult.PERMISSIONS_RATIONALE)
            else -> permissionCheck.launch(permissions.toTypedArray())
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(fragment.requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        permissionsRationale.addAll(permissions.filter {
            fragment.shouldShowRequestPermissionRationale(it)
        })
        return permissionsRationale.isNotEmpty()
    }

    private fun sendResult(permissionResult: PermissionResult) {
        block(permissionResult)
        permissions.clear()
        block = {}
        permissionsRationale.clear()
    }

}