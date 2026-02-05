package com.klim.trossage_android.data.websocket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private var client: OkHttpClient? = null
    private var webSocket: WebSocket? = null

    private const val BASE_WS_URL = "wss://trossage.teew.ru/api/ws"

    fun websocketFlow(authToken: String) = callbackFlow<String?> {
        Log.d("WebSocketManager", "Connecting to WS: $BASE_WS_URL token=${authToken.take(20)}...")

        val client = client ?: OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build().also { WebSocketManager.client = it }

        val request = Request.Builder()
            .url("$BASE_WS_URL?token=$authToken")
            .build()

        Log.d("WebSocketManager", "WS Request: ${request.url}")

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val status = response.code
                Log.d("WebSocketManager", "WS OPEN status=$status")
                WebSocketManager.webSocket = webSocket
                trySend("connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketManager", "WS message: $text")
                trySend(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketManager", "WS FAILURE", t)
                WebSocketManager.webSocket = null
                trySend(null)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebSocketManager", "WS CLOSING code=$code reason='$reason'")
                WebSocketManager.webSocket = null
                trySend(null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("WebSocketManager", "WS CLOSED code=$code reason='$reason'")
                WebSocketManager.webSocket = null
            }
        }

        val ws = client.newWebSocket(request, listener)
        awaitClose {
            Log.d("WebSocketManager", "WS awaitClose")
            ws.close(1000, "App closed")
            WebSocketManager.webSocket = null
        }
    }

    fun sendTyping(chatId: Int, operations: List<TypingOperation>) {
        val json = Gson().toJson(mapOf(
            "type" to "typing",
            "chat_id" to chatId,
            "operations" to operations
        ))
        Log.d("WebSocketManager", "WS SEND TYPING chatId=$chatId: $json")
        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }

    data class TypingOperation(
        val type: String,
        val position: Int? = null,
        val length: Int? = null,
        val text: String? = null
    )
}
