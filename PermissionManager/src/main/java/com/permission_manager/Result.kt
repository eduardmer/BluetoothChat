package com.permission_manager

enum class PermissionResult {
    /**
     * this is returned when all permissions have been granted
     */
    PERMISSIONS_GRANTED,

    /**
     * this is returned when at least one of permissions has been denied
     */
    PERMISSIONS_DENIED,

    /**
     * this is returned when you should show UI with rationale for some permissions
     */
    PERMISSIONS_RATIONALE
}