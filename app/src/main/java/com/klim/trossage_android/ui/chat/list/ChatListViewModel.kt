package com.klim.trossage_android.ui.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.ChatRepository
import com.klim.trossage_android.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 20

    private var searchJob: Job? = null
    private var searchOffset = 0
    private val searchPageSize = 20

    init {
        loadChats()
    }

    fun loadChats() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            chatRepository.loadChats(currentOffset, pageSize)
                .onSuccess { newChats ->
                    val currentChats = _uiState.value.chats
                    _uiState.value = _uiState.value.copy(
                        chats = if (currentOffset == 0) newChats else currentChats + newChats,
                        isLoading = false,
                        hasMore = newChats.size == pageSize
                    )
                    currentOffset += newChats.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки чатов"
                    )
                }
        }
    }

    fun refresh() {
        currentOffset = 0
        _uiState.value = _uiState.value.copy(
            chats = emptyList(),
            error = null
        )
        loadChats()
    }

    fun searchUsers(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            searchOffset = 0
            _searchState.value = SearchUiState()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            searchOffset = 0
            _searchState.value = _searchState.value.copy(
                isLoading = true,
                query = query
            )

            userRepository.searchUsers(query, searchPageSize, searchOffset)
                .onSuccess { users ->
                    _searchState.value = _searchState.value.copy(
                        users = users,
                        isLoading = false,
                        hasMore = users.size == searchPageSize,
                        error = null
                    )
                    searchOffset += users.size
                }
                .onFailure { error ->
                    _searchState.value = _searchState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка поиска"
                    )
                }
        }
    }

    fun loadMoreSearchResults() {
        val currentState = _searchState.value
        if (currentState.isLoading || !currentState.hasMore || currentState.query.isBlank()) return

        _searchState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            userRepository.searchUsers(currentState.query, searchPageSize, searchOffset)
                .onSuccess { newUsers ->
                    val updatedUsers = currentState.users + newUsers
                    _searchState.value = _searchState.value.copy(
                        users = updatedUsers,
                        isLoadingMore = false,
                        hasMore = newUsers.size == searchPageSize
                    )
                    searchOffset += newUsers.size
                }
                .onFailure { error ->
                    _searchState.value = _searchState.value.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Ошибка загрузки"
                    )
                }
        }
    }

    fun createChat(companionUserId: Int, onSuccess: (Int, String) -> Unit) {
        viewModelScope.launch {
            chatRepository.createChat(companionUserId)
                .onSuccess { chat ->
                    onSuccess(chat.chatId, chat.companionDisplayName)
                    refresh()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Ошибка создания чата"
                    )
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)

data class SearchUiState(
    val users: List<User> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null
)
