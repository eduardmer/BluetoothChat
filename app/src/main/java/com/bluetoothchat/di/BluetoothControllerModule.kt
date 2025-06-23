package com.bluetoothchat.di

import com.bluetoothchat.data.BluetoothControllerImpl
import com.bluetoothchat.data.ChatRepositoryImpl
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.ChatRepository
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

    @Binds
    @Singleton
    fun provideChatRepositoryImpl(chatRepository: ChatRepositoryImpl): ChatRepository

}