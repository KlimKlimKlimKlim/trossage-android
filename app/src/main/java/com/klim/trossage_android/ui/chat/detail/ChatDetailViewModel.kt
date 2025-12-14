package com.klim.trossage_android.ui.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatDetailViewModel(
    private val chatId: String,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState())
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var currentOffset = 0

    init {
        loadMessages()
        observeRealtimeUpdates()
    }

    fun loadMessages() {
        if (_uiState.value.isLoadingMessages) return

        _uiState.value = _uiState.value.copy(isLoadingMessages = true)

        viewModelScope.launch {
            messageRepository.getMessages(chatId, currentOffset, limit = 10)
                .onSuccess { newMessages ->
                    _uiState.value = _uiState.value.copy(
                        messages = newMessages + _uiState.value.messages,
                        isLoadingMessages = false,
                        hasMoreMessages = newMessages.size == 10
                    )
                    currentOffset += newMessages.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMessages = false,
                        error = error.message
                    )
                }
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            messageRepository.getMessages(chatId, offset = 0, limit = 10)
                .onSuccess { latestMessages ->
                    val existingIds = _uiState.value.messages.map { it.messageId }.toSet()
                    val newMessages = latestMessages.filter { it.messageId !in existingIds }

                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + newMessages
                    )
                }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(currentInput = text)

        messageRepository.sendTypingUpdate(chatId, text)
    }

    fun sendMessage() {
        val text = _uiState.value.currentInput.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(isSendingMessage = true)

        viewModelScope.launch {
            messageRepository.sendMessage(chatId, text)
                .onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + message,
                        currentInput = "",
                        isSendingMessage = false
                    )

                    messageRepository.sendTypingUpdate(chatId, "")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingMessage = false,
                        error = error.message
                    )
                }
        }
    }

    private fun observeRealtimeUpdates() {
        messageRepository.connectWebSockets(chatId)

        viewModelScope.launch {
            messageRepository.observeNewMessages(chatId).collect { message ->
                val existingIds = _uiState.value.messages.map { it.messageId }.toSet()
                if (message.messageId !in existingIds) {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + message
                    )
                }
            }
        }

        viewModelScope.launch {
            messageRepository.observeTyping(chatId).collect { typingText ->
                _uiState.value = _uiState.value.copy(companionTypingText = typingText)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageRepository.disconnectWebSockets()
    }
}

data class ChatDetailUiState(
    val messages: List<Message> = emptyList(),
    val currentInput: String = "",
    val companionTypingText: String = "",
    val isLoadingMessages: Boolean = false,
    val isSendingMessage: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val error: String? = null
)