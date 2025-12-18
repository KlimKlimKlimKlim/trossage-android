package com.klim.trossage_android.ui.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatId: String,
    private val companionName: String,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState(companionName = companionName))
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 50

    init {
        loadMessages()
        // TODO: когда будет WebSocket - раскомментировать
        // observeRealtimeUpdates()
    }

    private fun loadMessages() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            messageRepository.loadMessages(chatId, currentOffset, pageSize)
                .onSuccess { newMessages ->
                    val currentMessages = _uiState.value.messages
                    _uiState.value = _uiState.value.copy(
                        messages = newMessages + currentMessages,
                        isLoading = false,
                        hasMore = newMessages.size == pageSize
                    )
                    currentOffset += newMessages.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки сообщений"
                    )
                }
        }
    }

    fun loadMoreMessages() {
        loadMessages()
    }

    fun onMessageTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)

        // TODO: когда будет WebSocket - раскомментировать
        // if (text.isNotEmpty()) {
        //     messageRepository.sendTypingUpdate(chatId, true)
        // }
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty()) return

        _uiState.value = _uiState.value.copy(isSending = true, error = null)

        viewModelScope.launch {
            messageRepository.sendMessage(chatId, text)
                .onSuccess { message ->
                    val updatedMessages = _uiState.value.messages + message
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        messageText = "",
                        isSending = false
                    )
                    // TODO: когда будет WebSocket
                    // messageRepository.sendTypingUpdate(chatId, false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = error.message ?: "Ошибка отправки сообщения"
                    )
                }
        }
    }

    private fun observeRealtimeUpdates() {
        // TODO: когда будет WebSocket - раскомментировать
        // viewModelScope.launch {
        //     messageRepository.connectWebSockets(chatId)
        //
        //     launch {
        //         messageRepository.observeNewMessages(chatId).collect { message ->
        //             if (message.chatId == chatId &&
        //                 _uiState.value.messages.none { it.messageId == message.messageId }) {
        //                 val updatedMessages = _uiState.value.messages + message
        //                 _uiState.value = _uiState.value.copy(messages = updatedMessages)
        //             }
        //         }
        //     }
        //
        //     launch {
        //         messageRepository.observeTyping(chatId).collect { isTyping ->
        //             _uiState.value = _uiState.value.copy(companionIsTyping = isTyping)
        //         }
        //     }
        // }
    }

    override fun onCleared() {
        super.onCleared()
        // TODO: когда будет WebSocket
        // messageRepository.disconnectWebSockets()
    }
}

data class ChatDetailUiState(
    val companionName: String,
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val hasMore: Boolean = true,
    val companionIsTyping: Boolean = false,
    val error: String? = null
)