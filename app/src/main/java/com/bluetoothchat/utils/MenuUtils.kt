package com.bluetoothchat.utils

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

inline fun Fragment.addMenu(
    menuId: Int,
    state: Lifecycle.State = Lifecycle.State.RESUMED,
    crossinline onMenuItemSelected: (MenuItem) -> Boolean
) {
    requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(menuId, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return onMenuItemSelected(menuItem)
        }

    }, viewLifecycleOwner, state)
}