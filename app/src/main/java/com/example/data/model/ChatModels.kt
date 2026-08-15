package com.example.data.model

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MediaType {
    TEXT,
    IMAGE,
    VOICE,
    FILE,
    LOCATION
}

data class ChatUser(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String? = null,
    val avatarColorIndex: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: String = "hozir onlayn",
    val bio: String = "Saburov Chat foydalanuvchisi",
    val isVerified: Boolean = false,
    val statusEmoji: String? = null
)

data class ChatMessage(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMine: Boolean = true,
    val status: MessageStatus = MessageStatus.SENT,
    val mediaType: MediaType = MediaType.TEXT,
    val mediaUrl: String? = null,
    val voiceDurationSeconds: Int = 0,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val isPinned: Boolean = false
)

data class ChatRoom(
    val id: String,
    val title: String,
    val avatarUrl: String? = null,
    val avatarColorIndex: Int = 0,
    val isGroup: Boolean = false,
    val isChannel: Boolean = false,
    val isBot: Boolean = false,
    val isVerified: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessage: ChatMessage? = null,
    val membersCount: Int = 1,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val typingUser: String? = null,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val description: String = "",
    val username: String = ""
)

data class ServerApiStatus(
    val baseUrl: String = "https://api.saburov.uz",
    val isConnected: Boolean = true,
    val pingMs: Long = 28,
    val activeProtocol: String = "WebSocket + REST (v1)",
    val lastSyncTime: String = "Hozir",
    val autoSync: Boolean = true
)
