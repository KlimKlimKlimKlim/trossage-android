package com.klim.trossage_android.ui.chat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.websocket.WebSocketManager
import com.klim.trossage_android.domain.model.Message
import com.klim.trossage_android.domain.model.MessageStatus
import com.klim.trossage_android.domain.repository.MessageRepository
import com.klim.trossage_android.domain.repository.TypingOperation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.JsonParser
import com.google.gson.Gson
import android.util.Log
import com.klim.trossage_android.data.util.DateUtils
import kotlin.random.Random

class ChatDetailViewModel(
    private val chatId: Int,
    private val companionName: String,
    private val messageRepository: MessageRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatDetailUiState(companionName = companionName))
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 50
    private var isLoadingMore = false
    private var websocketJob: Job? = null
    private var typingJob: Job? = null
    private var hasValidToken = false

    private var previousTypingText = ""
    private val pendingOperations = mutableListOf<TypingOperation>()

    init {
        val token = authPrefs.getAccessToken()
        Log.d("ChatVM", "ChatDetailViewModel chatId=$chatId")
        Log.d("ChatVM", "TOKEN: ${if (token.isNullOrBlank()) "NULL" else token!!.take(20)}...")

        if (token.isNullOrBlank()) {
            Log.e("ChatVM", "NO TOKEN - WebSocket NOT started")
        } else {
            Log.d("ChatVM", "TOKEN OK - starting WebSocket")
            hasValidToken = true
        }

        loadMessages()
        if (hasValidToken) {
            connectWebSocket()
        }
    }

    private fun connectWebSocket() {
        websocketJob?.cancel()
        websocketJob = viewModelScope.launch {
            Log.d("ChatVM", "WebSocketFlow started for chatId=$chatId")
            WebSocketManager.websocketFlow(authPrefs.getAccessToken()!!).collect { message ->
                Log.d("ChatVM", "WS message: $message")

                when {
                    message == null -> {
                        Log.d("ChatVM", "WS disconnected")
                        _uiState.value = _uiState.value.copy(
                            companionTypingText = "",
                            companionIsTyping = false
                        )
                    }
                    message == "connected" -> {
                        Log.d("ChatVM", "WS connected")
                    }
                    message.contains("\"type\":\"typing\"") -> {
                        handleTypingMessage(message)
                    }
                    message.contains("\"type\":\"new_message\"") -> {
                        handleNewMessage(message)
                    }
                    else -> {
                        Log.d("ChatVM", "Unknown message: $message")
                    }
                }
            }
        }
    }

    private fun handleTypingMessage(message: String) {
        try {
            val jsonObject = JsonParser.parseString(message).asJsonObject
            val data = jsonObject.getAsJsonObject("data")
            val msgChatId = data?.get("chat_id")?.asInt ?: jsonObject.get("chat_id")?.asInt ?: return

            Log.d("ChatVM", "Typing message for chat $msgChatId, current chat $chatId")

            if (msgChatId != chatId) return

            val operations = data?.getAsJsonArray("operations") ?: jsonObject.getAsJsonArray("operations")
            if (operations != null && !operations.isJsonNull && operations.size() > 0) {
                Log.d("ChatVM", "Applying ${operations.size()} typing operations")
                applyTypingOperations(operations)
            }
        } catch (e: Exception) {
            Log.e("ChatVM", "Parse typing error: $message", e)
        }
    }

    private fun handleNewMessage(message: String) {
        try {
            val jsonObject = JsonParser.parseString(message).asJsonObject
            val data = jsonObject.getAsJsonObject("data")
            val msgChatId = data?.get("chat_id")?.asInt ?: return
            val senderId = data?.get("sender_id")?.asInt ?: return
            val messageText = data?.get("text")?.asString ?: return
            val messageId = data?.get("id")?.asInt ?: return
            val createdAt = data?.get("created_at")?.asString ?: return

            Log.d("ChatVM", "New message for chat $msgChatId, current chat $chatId")

            if (msgChatId != chatId) return

            val currentUserId = authPrefs.getCurrentUser()?.userId?.toIntOrNull() ?: 0
            val isMine = senderId == currentUserId

            val senderName = if (isMine) {
                authPrefs.getCurrentUser()?.displayName ?: ""
            } else {
                companionName
            }

            val newMessage = Message(
                messageId = messageId,
                chatId = msgChatId,
                senderId = senderId,
                senderDisplayName = senderName,
                text = messageText,
                timestamp = DateUtils.parseIsoToMillis(createdAt),
                isMine = isMine,
                status = MessageStatus.SENT
            )

            val existingMessages = _uiState.value.messages
            if (existingMessages.none { it.messageId == messageId }) {
                _uiState.value = _uiState.value.copy(
                    messages = existingMessages + newMessage,
                    companionTypingText = "",
                    companionIsTyping = false
                )
                Log.d("ChatVM", "Added new message, total now: ${_uiState.value.messages.size}")
            }
        } catch (e: Exception) {
            Log.e("ChatVM", "Parse new_message error: $message", e)
        }
    }

    private fun applyTypingOperations(operations: com.google.gson.JsonArray) {
        var currentText = _uiState.value.companionTypingText
        for (op in operations) {
            val opObj = op.asJsonObject
            val type = opObj.get("type")?.asString ?: continue
            try {
                when (type) {
                    "insert" -> {
                        val position = opObj.get("position")?.asInt ?: 0
                        val text = opObj.get("text")?.asString ?: ""
                        if (position <= currentText.length) {
                            currentText = currentText.substring(0, position) + text + currentText.substring(position)
                        }
                    }
                    "delete" -> {
                        val position = opObj.get("position")?.asInt ?: 0
                        val length = opObj.get("length")?.asInt ?: 0
                        if (position + length <= currentText.length) {
                            currentText = currentText.substring(0, position) + currentText.substring(position + length)
                        }
                    }
                    "replace" -> {
                        val position = opObj.get("position")?.asInt ?: 0
                        val length = opObj.get("length")?.asInt ?: 0
                        val text = opObj.get("text")?.asString ?: ""
                        if (position + length <= currentText.length) {
                            currentText = currentText.substring(0, position) + text + currentText.substring(position + length)
                        }
                    }
                    "clear" -> {
                        currentText = ""
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatVM", "Error applying typing operation: $type", e)
            }
        }
        Log.d("ChatVM", "Applied operations, new text: '$currentText'")
        handleTypingEvent(currentText)
    }


    private fun handleTypingEvent(text: String) {
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                companionTypingText = "",
                companionIsTyping = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                companionTypingText = text,
                companionIsTyping = true
            )
        }
        Log.d("ChatVM", "Typing state: isTyping=${_uiState.value.companionIsTyping} text='${_uiState.value.companionTypingText}'")
    }

    private fun loadMessages() {
        if (isLoadingMore) return

        isLoadingMore = true
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            messageRepository.loadMessages(chatId, currentOffset, pageSize, companionName)
                .onSuccess { newMessages ->
                    val currentMessages = _uiState.value.messages
                    _uiState.value = _uiState.value.copy(
                        messages = if (currentOffset == 0) newMessages else newMessages + currentMessages,
                        isLoading = false,
                        hasMore = newMessages.size == pageSize
                    )
                    currentOffset += newMessages.size
                    Log.d("ChatVM", "Loaded ${newMessages.size} messages, total now: ${_uiState.value.messages.size}")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка загрузки сообщений"
                    )
                }
            isLoadingMore = false
        }
    }

    fun loadMoreMessages() {
        loadMessages()
    }

    fun refreshMessages() {
        currentOffset = 0
        _uiState.value = _uiState.value.copy(messages = emptyList())
        loadMessages()
    }

    fun onMessageTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
        generateTypingOperations(text)
    }

    private fun generateTypingOperations(text: String) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            val operations = calculateSimpleDiff(previousTypingText, text)
            pendingOperations.clear()
            pendingOperations.addAll(operations)
            delay(300)
            if (pendingOperations.isNotEmpty()) {
                Log.d("ChatVM", "===== TYPING REQUEST =====")
                Log.d("ChatVM", "chatId: $chatId")
                Log.d("ChatVM", "operations: $pendingOperations")
                Log.d("ChatVM", "JSON: ${Gson().toJson(pendingOperations)}")

                val result = try {
                    messageRepository.sendTyping(chatId, pendingOperations.toList())
                    "SUCCESS"
                } catch (e: Exception) {
                    "ERROR: ${e.message}"
                }

                Log.d("ChatVM", "Result: $result")
                Log.d("ChatVM", "==========================")
                pendingOperations.clear()
            }
            previousTypingText = text
        }
    }

    private fun calculateSimpleDiff(oldText: String, newText: String): List<TypingOperation> {
        if (oldText == newText) return emptyList()

        when {
            newText.isEmpty() -> {
                return listOf(TypingOperation(type = "clear"))
            }
            oldText.isEmpty() -> {
                return listOf(
                    TypingOperation(
                        type = "insert",
                        position = 0,
                        text = newText
                    )
                )
            }
            newText.startsWith(oldText) -> {
                val addedText = newText.substring(oldText.length)
                return listOf(
                    TypingOperation(
                        type = "insert",
                        position = oldText.length,
                        text = addedText
                    )
                )
            }
            oldText.startsWith(newText) -> {
                val deletedLength = oldText.length - newText.length
                return listOf(
                    TypingOperation(
                        type = "delete",
                        position = newText.length,
                        length = deletedLength
                    )
                )
            }
            else -> {
                return listOf(
                    TypingOperation(
                        type = "replace",
                        position = 0,
                        length = oldText.length,
                        text = newText
                    )
                )
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty()) return

        val currentUserId = authPrefs.getCurrentUser()?.userId?.toIntOrNull() ?: 0
        val currentUserName = authPrefs.getCurrentUser()?.displayName ?: ""

        val tempMessage = Message(
            messageId = Random.nextInt(Int.MIN_VALUE, -1),
            chatId = chatId,
            senderId = currentUserId,
            senderDisplayName = currentUserName,
            text = text,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            status = MessageStatus.SENDING
        )

        val updatedMessages = _uiState.value.messages + tempMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            messageText = "",
            isSending = true,
            error = null
        )
        previousTypingText = ""
        pendingOperations.clear()

        viewModelScope.launch {
            messageRepository.sendMessage(chatId, text)
                .onSuccess { message ->
                    val messagesWithoutTemp = _uiState.value.messages.filter { it.messageId != tempMessage.messageId }
                    if (messagesWithoutTemp.none { it.messageId == message.messageId }) {
                        _uiState.value = _uiState.value.copy(
                            messages = messagesWithoutTemp + message,
                            isSending = false,
                            companionTypingText = "",
                            companionIsTyping = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            messages = messagesWithoutTemp,
                            isSending = false,
                            companionTypingText = "",
                            companionIsTyping = false
                        )
                    }
                }
                .onFailure { error ->
                    val messagesWithFailed = _uiState.value.messages.map {
                        if (it.messageId == tempMessage.messageId) {
                            it.copy(status = MessageStatus.FAILED)
                        } else it
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = messagesWithFailed,
                        isSending = false,
                        error = error.message ?: "Ошибка отправки сообщения"
                    )
                }
        }
    }


    override fun onCleared() {
        super.onCleared()
        websocketJob?.cancel()
        typingJob?.cancel()
        WebSocketManager.disconnect()
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
    val companionTypingText: String = "",
    val error: String? = null
)
