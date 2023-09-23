package com.bluetoothchat.di

import com.bluetoothchat.data.BluetoothControllerImpl
import com.bluetoothchat.domain.BluetoothController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BluetoothControllerModule {

    @Binds
    @Singleton
    fun provideBluetoothController(bluetoothController: BluetoothControllerImpl): BluetoothController

}