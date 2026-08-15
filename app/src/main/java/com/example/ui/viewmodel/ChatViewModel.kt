package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.MediaType
import com.example.data.model.ServerApiStatus
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ChatFilter(val title: String) {
    ALL("Barchasi"),
    DIRECT("Shaxsiy"),
    GROUPS("Guruhlar"),
    CHANNELS("Kanallar"),
    BOTS("Botlar")
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(database, viewModelScope)

    val apiStatus: StateFlow<ServerApiStatus> = repository.apiStatus
    val typingStatus: StateFlow<Map<String, String>> = repository.typingStatus

    private val _selectedFilter = MutableStateFlow(ChatFilter.ALL)
    val selectedFilter: StateFlow<ChatFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeChat = MutableStateFlow<ChatRoom?>(null)
    val activeChat: StateFlow<ChatRoom?> = _activeChat.asStateFlow()

    private val _replyingMessage = MutableStateFlow<ChatMessage?>(null)
    val replyingMessage: StateFlow<ChatMessage?> = _replyingMessage.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _voiceSeconds = MutableStateFlow(0)
    val voiceSeconds: StateFlow<Int> = _voiceSeconds.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Sleek dark theme by default
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private var voiceTimerJob: Job? = null

    val rawRooms: StateFlow<List<ChatRoom>> = repository.getAllRooms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredRooms: StateFlow<List<ChatRoom>> = combine(
        rawRooms,
        _selectedFilter,
        _searchQuery
    ) { rooms, filter, query ->
        rooms.filter { room ->
            val matchesFilter = when (filter) {
                ChatFilter.ALL -> true
                ChatFilter.DIRECT -> !room.isGroup && !room.isChannel && !room.isBot
                ChatFilter.GROUPS -> room.isGroup
                ChatFilter.CHANNELS -> room.isChannel
                ChatFilter.BOTS -> room.isBot
            }
            val matchesQuery = if (query.isBlank()) true else {
                room.title.contains(query, ignoreCase = true) ||
                        room.description.contains(query, ignoreCase = true) ||
                        room.username.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentMessages: StateFlow<List<ChatMessage>> = _activeChatId.flatMapLatest { chatId ->
        if (chatId == null) flowOf(emptyList()) else repository.getMessagesForChat(chatId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: ChatFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun openChat(room: ChatRoom) {
        _activeChatId.value = room.id
        _activeChat.value = room
        _replyingMessage.value = null
        viewModelScope.launch {
            repository.markRoomAsRead(room.id)
        }
    }

    fun closeChat() {
        _activeChatId.value = null
        _activeChat.value = null
        _replyingMessage.value = null
    }

    fun setReplyingTo(message: ChatMessage?) {
        _replyingMessage.value = message
    }

    fun sendMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank()) return

        val reply = _replyingMessage.value
        _replyingMessage.value = null

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text.trim(),
                mediaType = MediaType.TEXT,
                replyTo = reply
            )
        }
    }

    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _voiceSeconds.value = 0
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _voiceSeconds.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        _voiceSeconds.value = 0
    }

    fun finishVoiceRecording() {
        val chatId = _activeChatId.value ?: return
        val duration = _voiceSeconds.value
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        _voiceSeconds.value = 0

        if (duration < 1) return

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = "🎤 Ovozli xabar (${duration}s)",
                mediaType = MediaType.VOICE,
                voiceDurationSeconds = duration
            )
        }
    }

    fun sendSampleImage(imageUrl: String, caption: String = "") {
        val chatId = _activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = caption.ifBlank { "📷 Rasm" },
                mediaType = MediaType.IMAGE,
                mediaUrl = imageUrl
            )
        }
    }

    fun toggleReaction(message: ChatMessage, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(message.id, emoji)
        }
    }

    fun togglePinMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.togglePinMessage(message.id)
        }
    }

    fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.deleteMessage(message.id)
        }
    }

    fun togglePinRoom(room: ChatRoom) {
        viewModelScope.launch {
            repository.togglePinRoom(room.id)
        }
    }

    fun createNewChat(title: String, description: String, isGroup: Boolean, isChannel: Boolean, username: String) {
        viewModelScope.launch {
            val newId = repository.createNewRoom(title, description, isGroup, isChannel, username)
            // Open the new room
            val room = rawRooms.value.find { it.id == newId } ?: ChatRoom(
                id = newId,
                title = title,
                isGroup = isGroup,
                isChannel = isChannel,
                description = description,
                username = username
            )
            openChat(room)
        }
    }

    fun refreshApiConnection(customUrl: String = "https://api.saburov.uz") {
        viewModelScope.launch {
            repository.testApiConnection(customUrl)
        }
    }
}
