package com.example.data.network

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID

sealed class SocketEvent {
    data class Connected(val url: String) : SocketEvent()
    data class Disconnected(val reason: String) : SocketEvent()
    data class Error(val message: String) : SocketEvent()
    data class NewMessage(val message: ChatMessage) : SocketEvent()
    data class UserTyping(val chatId: String, val userName: String) : SocketEvent()
}

class WebSocketManager(private val coroutineScope: CoroutineScope) {
    private var webSocket: WebSocket? = null
    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SocketEvent> = _events.asSharedFlow()

    private var isConnected = false

    fun connect(wsUrl: String = "wss://api.saburov.uz/ws") {
        try {
            val request = Request.Builder().url(wsUrl).build()
            webSocket = NetworkClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                    coroutineScope.launch {
                        _events.emit(SocketEvent.Connected(wsUrl))
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // Parse incoming websocket message
                    try {
                        val fakeMsg = ChatMessage(
                            id = UUID.randomUUID().toString(),
                            chatId = "general",
                            senderId = "remote",
                            senderName = "api.saburov.uz",
                            text = text,
                            timestamp = System.currentTimeMillis(),
                            isMine = false,
                            status = MessageStatus.READ,
                            mediaType = MediaType.TEXT
                        )
                        coroutineScope.launch {
                            _events.emit(SocketEvent.NewMessage(fakeMsg))
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocketManager", "Error parsing msg", e)
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                    coroutineScope.launch {
                        _events.emit(SocketEvent.Error(t.localizedMessage ?: "Connection error"))
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                    coroutineScope.launch {
                        _events.emit(SocketEvent.Disconnected(reason))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Connect failed", e)
            coroutineScope.launch {
                _events.emit(SocketEvent.Error("Server offline/unreachable"))
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (isConnected) {
            webSocket?.send("""{"type":"msg","chat_id":"$chatId","text":"$text"}""")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
        isConnected = false
    }
}
