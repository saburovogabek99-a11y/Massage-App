package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_rooms ORDER BY isPinned DESC, lastUpdated DESC")
    fun getAllRooms(): Flow<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE id = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: String): ChatRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<ChatRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: ChatRoomEntity)

    @Update
    suspend fun updateRoom(room: ChatRoomEntity)

    @Query("UPDATE chat_rooms SET isPinned = NOT isPinned WHERE id = :roomId")
    suspend fun togglePinRoom(roomId: String)

    @Query("UPDATE chat_rooms SET unreadCount = 0 WHERE id = :roomId")
    suspend fun markRoomAsRead(roomId: String)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForChat(chatId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("UPDATE chat_messages SET reactionsJson = :reactionsJson, myReaction = :myReaction WHERE id = :messageId")
    suspend fun updateMessageReaction(messageId: String, reactionsJson: String, myReaction: String?)

    @Query("UPDATE chat_messages SET isPinned = NOT isPinned WHERE id = :messageId")
    suspend fun togglePinMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun clearChatMessages(chatId: String)

    @Query("DELETE FROM chat_rooms WHERE id = :roomId")
    suspend fun deleteRoom(roomId: String)
}
