package com.klim.trossage_android.ui.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var currentOffset = 0

    init {
        loadChats()
        observeRealtimeUpdates()
    }

    fun loadChats() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            chatRepository.getChats(currentOffset, limit = 10)
                .onSuccess { newChats ->
                    _uiState.value = _uiState.value.copy(
                        chats = _uiState.value.chats + newChats,
                        isLoading = false,
                        hasMore = newChats.size == 10
                    )
                    currentOffset += newChats.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun refreshChats() {
        currentOffset = 0
        _uiState.value = _uiState.value.copy(chats = emptyList())
        loadChats()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            chatRepository.searchUsers(query)
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(searchResults = users)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(searchResults = emptyList())
                }
        }
    }

    fun openChatWithUser(userId: String, onChatOpened: (Chat) -> Unit) {
        viewModelScope.launch {
            chatRepository.createOrGetChat(userId)
                .onSuccess { chat ->
                    onChatOpened(chat)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteChat(chatId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        chats = _uiState.value.chats.filter { it.chatId != chatId }
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    private fun observeRealtimeUpdates() {
        chatRepository.connectWebSocket()

        viewModelScope.launch {
            chatRepository.observeNewMessages().collect { message ->
                updateChatWithNewMessage(message)
            }
        }

        viewModelScope.launch {
            chatRepository.observeChatUpdates().collect { updatedChat ->
                updateChat(updatedChat)
            }
        }
    }

    private fun updateChatWithNewMessage(message: Message) {
        val currentChats = _uiState.value.chats.toMutableList()

        val chatIndex = currentChats.indexOfFirst { it.chatId == message.chatId }

        if (chatIndex != -1) {
            val updatedChat = currentChats[chatIndex].copy(
                lastMessage = message.text,
                lastMessageTimestamp = message.timestamp,
                isRead = message.isMine
            )
            currentChats.removeAt(chatIndex)
            currentChats.add(0, updatedChat)
        }

        _uiState.value = _uiState.value.copy(chats = currentChats)
    }

    private fun updateChat(updatedChat: Chat) {
        val currentChats = _uiState.value.chats.toMutableList()
        val chatIndex = currentChats.indexOfFirst { it.chatId == updatedChat.chatId }

        if (chatIndex != -1) {
            currentChats[chatIndex] = updatedChat
            currentChats.sortByDescending { it.lastMessageTimestamp }
        } else {
            currentChats.add(0, updatedChat)
        }

        _uiState.value = _uiState.value.copy(chats = currentChats)
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.disconnectWebSocket()
    }
}

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList()
)