package com.bluetoothchat.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetoothchat.data.local.MessagesEntity
import com.bluetoothchat.domain.BluetoothController
import com.bluetoothchat.domain.ChatRepository
import com.bluetoothchat.model.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val bluetoothController: BluetoothController,
    private val chatRepository: ChatRepository
    ) : ViewModel() {

    init {
        bluetoothController.listenForMessages()?.onEach {
            chatRepository.saveMessage(it)
        }?.catch { error ->
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    error = error.message ?: "Error"
                )
            }
        }?.launchIn(viewModelScope)
    }

    private val _state = MutableStateFlow(State())
    val state = combine(
        chatRepository.getMessages(savedState["remoteDevice"] ?: ""),
        bluetoothController.connectionState,
        _state
    ) { messages, connectionState, state ->
        State(
            messages,
            connectionState is ConnectionState.ConnectionAccepted && connectionState.device.address == savedState["remoteDevice"],
            state.error
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        State()
    )



    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                bluetoothController.sendMessage(message)?.let {
                    chatRepository.saveMessage(it)
                }
            } catch (error: Exception) {
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(error = error.message ?: "Error")
                }
            }
        }
    }

}

data class State(
    val messages: List<MessagesEntity> = emptyList(),
    val isConnected: Boolean = false,
    val error: String? = null
)