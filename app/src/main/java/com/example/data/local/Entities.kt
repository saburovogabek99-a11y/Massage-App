package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey val id: String,
    val title: String,
    val avatarUrl: String?,
    val avatarColorIndex: Int,
    val isGroup: Boolean,
    val isChannel: Boolean,
    val isBot: Boolean,
    val isVerified: Boolean,
    val unreadCount: Int,
    val membersCount: Int,
    val isOnline: Boolean,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val description: String,
    val username: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isMine: Boolean,
    val status: String, // String representation of MessageStatus
    val mediaType: String, // String representation of MediaType
    val mediaUrl: String?,
    val voiceDurationSeconds: Int,
    val replyToMessageId: String?,
    val replyToText: String?,
    val replyToSender: String?,
    val reactionsJson: String, // e.g. "❤️:2,👍:1"
    val myReaction: String?,
    val isPinned: Boolean
)
