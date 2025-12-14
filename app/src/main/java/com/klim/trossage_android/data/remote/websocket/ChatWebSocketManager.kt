package com.klim.trossage_android.data.remote.websocket

import com.google.gson.Gson
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.dto.ChatDto
import com.klim.trossage_android.data.remote.dto.MessageDto
import com.klim.trossage_android.data.remote.dto.WebSocketMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*

class ChatWebSocketManager(
    private val authPreferences: AuthPreferences,
    private val baseUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    fun observeMessages(): Flow<MessageDto> = callbackFlow {
        connect { message ->
            if (message.type == "new_message") {
                try {
                    val messageDto = gson.fromJson(message.data, MessageDto::class.java)
                    trySend(messageDto)
                } catch (e: Exception) {
                    // Логирование ошибки парсинга
                }
            }
        }

        awaitClose { disconnect() }
    }

    fun observeChatUpdates(): Flow<ChatDto> = callbackFlow {
        connect { message ->
            if (message.type == "chat_update") {
                try {
                    val chatDto = gson.fromJson(message.data, ChatDto::class.java)
                    trySend(chatDto)
                } catch (e: Exception) {
                    // Логирование ошибки парсинга
                }
            }
        }

        awaitClose { disconnect() }
    }

    private fun connect(onMessage: (WebSocketMessage) -> Unit) {
        if (webSocket != null) return

        val token = authPreferences.getAccessToken() ?: return

        val request = Request.Builder()
            .url("$baseUrl/ws/message?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val wsMessage = gson.fromJson(text, WebSocketMessage::class.java)
                    onMessage(wsMessage)
                } catch (e: Exception) {
                    // Логирование ошибки
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@ChatWebSocketManager.webSocket = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                this@ChatWebSocketManager.webSocket = null
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }
}