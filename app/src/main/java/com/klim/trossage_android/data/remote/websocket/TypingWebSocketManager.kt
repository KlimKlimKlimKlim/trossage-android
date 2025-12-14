package com.klim.trossage_android.data.remote.websocket

import com.google.gson.Gson
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.dto.TypingMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*

class TypingWebSocketManager(
    private val authPreferences: AuthPreferences,
    private val baseUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()

    fun observeTyping(chatId: String): Flow<String> = callbackFlow {
        connect()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val typingMessage = gson.fromJson(text, TypingMessage::class.java)
                    // Фильтруем только сообщения из нужного чата
                    if (typingMessage.chatId == chatId) {
                        trySend(typingMessage.text)
                    }
                } catch (e: Exception) {
                    // Логирование ошибки парсинга
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            }
        }

        awaitClose { disconnect() }
    }

    fun sendTyping(chatId: String, text: String) {
        val typingMessage = TypingMessage(chatId, text)
        val json = gson.toJson(typingMessage)
        webSocket?.send(json)
    }

    private fun connect() {
        if (webSocket != null) return

        val token = authPreferences.getAccessToken() ?: return

        val request = Request.Builder()
            .url("$baseUrl/ws/typing?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@TypingWebSocketManager.webSocket = null
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                this@TypingWebSocketManager.webSocket = null
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }
}