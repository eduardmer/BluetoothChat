package com.bluetoothchat.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(BluetoothManager::class.java)
        } else {
            ContextCompat.getSystemService(context, BluetoothManager::class.java)
        }
    }

    @Provides
    @Singleton
    fun provideBluetoothAdapter(bluetoothManager: BluetoothManager?): BluetoothAdapter? {
        return bluetoothManager?.adapter
    }

}