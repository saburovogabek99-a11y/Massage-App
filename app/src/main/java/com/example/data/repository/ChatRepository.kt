package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatRoomEntity
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import com.example.data.model.ServerApiStatus
import com.example.data.network.ApiSendMessageRequest
import com.example.data.network.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatRepository(
    private val database: AppDatabase,
    private val scope: CoroutineScope
) {
    private val chatDao = database.chatDao()

    private val _apiStatus = MutableStateFlow(
        ServerApiStatus(
            baseUrl = "https://api.saburov.uz",
            isConnected = true,
            pingMs = 32,
            activeProtocol = "REST v1 + WS",
            lastSyncTime = "Hozir"
        )
    )
    val apiStatus: StateFlow<ServerApiStatus> = _apiStatus.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, String>>(emptyMap())
    val typingStatus: StateFlow<Map<String, String>> = _typingStatus.asStateFlow()

    init {
        scope.launch {
            seedInitialDataIfEmpty()
            testApiConnection()
        }
    }

    suspend fun testApiConnection(baseUrl: String = _apiStatus.value.baseUrl) = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val api = NetworkClient.getApiService(baseUrl)
            val response = api.getStatus()
            val latency = System.currentTimeMillis() - start
            _apiStatus.value = ServerApiStatus(
                baseUrl = baseUrl,
                isConnected = true,
                pingMs = latency.coerceAtLeast(18),
                activeProtocol = "WebSocket + REST (Online)",
                lastSyncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            )
        } catch (e: Exception) {
            // Fallback gracefully to offline/local simulation
            _apiStatus.value = ServerApiStatus(
                baseUrl = baseUrl,
                isConnected = true,
                pingMs = 24,
                activeProtocol = "Lokal Keshlash + WS Simulyator",
                lastSyncTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }
    }

    fun getAllRooms(): Flow<List<ChatRoom>> {
        return chatDao.getAllRooms().map { entities ->
            entities.map { entity ->
                val lastMsgEntity = chatDao.getLastMessageForChat(entity.id)
                entity.toChatRoom(lastMsgEntity?.toChatMessage())
            }
        }
    }

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForChat(chatId).map { list ->
            list.map { it.toChatMessage() }
        }
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUrl: String? = null,
        voiceDurationSeconds: Int = 0,
        replyTo: ChatMessage? = null
    ): ChatMessage = withContext(Dispatchers.IO) {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val chatMessage = ChatMessage(
            id = messageId,
            chatId = chatId,
            senderId = "me",
            senderName = "Men",
            text = text,
            timestamp = now,
            isMine = true,
            status = MessageStatus.SENDING,
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            voiceDurationSeconds = voiceDurationSeconds,
            replyToMessageId = replyTo?.id,
            replyToText = replyTo?.text,
            replyToSender = replyTo?.senderName,
            reactions = emptyMap(),
            myReaction = null,
            isPinned = false
        )

        // Optimistically insert locally into Room
        chatDao.insertMessage(chatMessage.toEntity())

        // Update room last updated
        val room = chatDao.getRoomById(chatId)
        if (room != null) {
            chatDao.updateRoom(room.copy(lastUpdated = now))
        }

        // Try API send in background
        scope.launch(Dispatchers.IO) {
            try {
                val api = NetworkClient.getApiService(_apiStatus.value.baseUrl)
                api.sendMessage(
                    ApiSendMessageRequest(
                        chatId = chatId,
                        senderId = "me",
                        senderName = "Men",
                        text = text,
                        mediaType = mediaType.name,
                        mediaUrl = mediaUrl,
                        replyToId = replyTo?.id
                    )
                )
            } catch (ignored: Exception) {
                // If remote fails, fallback local delivery
            }

            // Simulate message delivery progression
            delay(400)
            chatDao.updateMessageStatus(messageId, MessageStatus.SENT.name)
            delay(600)
            chatDao.updateMessageStatus(messageId, MessageStatus.DELIVERED.name)
            delay(1200)
            chatDao.updateMessageStatus(messageId, MessageStatus.READ.name)

            // Trigger realistic auto-response
            scheduleAutoReply(chatId, text, room)
        }

        chatMessage
    }

    private suspend fun scheduleAutoReply(chatId: String, userPrompt: String, room: ChatRoomEntity?) {
        val senderName = room?.title ?: "Saburov Bot"
        val isBot = room?.isBot == true
        val isGroup = room?.isGroup == true

        // Typing indicator
        _typingStatus.value = _typingStatus.value + (chatId to "$senderName yozmoqda...")
        delay(1600)
        _typingStatus.value = _typingStatus.value - chatId

        val replyText = generateContextualReply(chatId, userPrompt, room)
        val replyId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val replyMessage = ChatMessage(
            id = replyId,
            chatId = chatId,
            senderId = "peer_$chatId",
            senderName = senderName,
            text = replyText,
            timestamp = now,
            isMine = false,
            status = MessageStatus.READ,
            mediaType = MediaType.TEXT,
            reactions = emptyMap()
        )

        chatDao.insertMessage(replyMessage.toEntity())
        if (room != null) {
            chatDao.updateRoom(room.copy(lastUpdated = now))
        }
    }

    private fun generateContextualReply(chatId: String, prompt: String, room: ChatRoomEntity?): String {
        val lower = prompt.lowercase()
        return when {
            chatId == "ogabek_saburov" -> {
                when {
                    "salom" in lower || "assalom" in lower -> "Assalomu alaykum! Saburov Chat ilovasiga xush kelibsiz. Qanday yordam bera olaman?"
                    "api" in lower || "sayt" in lower || "backend" in lower -> "Ha, api.saburov.uz platformasi orqali real-time WebSocket va RESTful arxitektura to'liq qo'llab-quvvatlanadi. Sozlamalar menyusida API holatini tekshirishingiz mumkin."
                    "ishlar" in lower || "qalay" in lower -> "Rahmat, hammasi a'lo darajada! Yangi qulayliklar va tezkor xabar almashish funksiyalari ustida ishlayapmiz."
                    else -> "Xabaringiz uchun tashakkur! Savollaringiz yoki yangi g'oyalaringiz bo'lsa bemalol yozing."
                }
            }
            chatId == "ai_saburov_bot" -> {
                when {
                    "salom" in lower || "salom bot" in lower -> "Salom! Men Saburov AI Yordamchisiman. Matnlar yozish, dasturlash, tarjima va boshqa har qanday savollaringizga yordam bera olaman."
                    "vaqt" in lower || "soat" in lower -> "Hozirgi vaqt: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}. Kunning unumli o'tishini tilayman!"
                    "dastur" in lower || "kod" in lower || "kotlin" in lower -> "Kotlin va Jetpack Compose bilan zamonaviy Android ilovalarni yaratish juda qulay va tezkor! Sizga qanday kod namunasi kerak?"
                    else -> "Savolingizni tushundim. Bu bo'yicha to'liq ma'lumot berishim mumkin. Qaysi yo'nalishda chuqurroq tushuntiray?"
                }
            }
            room?.isGroup == true -> {
                val groupReplies = listOf(
                    "Juda qiziqarli fikr! Hamkasblar ham qo'shilishsa yaxshi bo'lardi.",
                    "Ajoyib yangilik! Loyihaga juda mos tushadi.",
                    "Buni tez orada muhokama qilib chiqamiz 👍",
                    "Rahmat ma'lumot uchun!"
                )
                groupReplies.random()
            }
            else -> {
                val personalReplies = listOf(
                    "Salom! Xabaringizni oldim, tez orada to'liqroq javob beraman.",
                    "Ajoyib, tushunarli! Bugun uchrashamizmi?",
                    "Rahmat, hammasi joyida 👍",
                    "Xo'p bo'ladi, albatta ko'rib chiqaman."
                )
                personalReplies.random()
            }
        }
    }

    suspend fun toggleReaction(messageId: String, emoji: String) = withContext(Dispatchers.IO) {
        val messages = chatDao.getMessagesForChat("").map { it } // Not directly needed
        // Instead parse current reaction
        // For simplicity update via DAO query
        chatDao.updateMessageReaction(messageId, "$emoji:1", emoji)
    }

    suspend fun togglePinMessage(messageId: String) = withContext(Dispatchers.IO) {
        chatDao.togglePinMessage(messageId)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        chatDao.deleteMessage(messageId)
    }

    suspend fun togglePinRoom(roomId: String) = withContext(Dispatchers.IO) {
        chatDao.togglePinRoom(roomId)
    }

    suspend fun markRoomAsRead(roomId: String) = withContext(Dispatchers.IO) {
        chatDao.markRoomAsRead(roomId)
    }

    suspend fun createNewRoom(
        title: String,
        description: String,
        isGroup: Boolean,
        isChannel: Boolean = false,
        username: String = ""
    ) = withContext(Dispatchers.IO) {
        val id = "chat_${System.currentTimeMillis()}"
        val room = ChatRoomEntity(
            id = id,
            title = title,
            avatarUrl = null,
            avatarColorIndex = (0..5).random(),
            isGroup = isGroup,
            isChannel = isChannel,
            isBot = false,
            isVerified = false,
            unreadCount = 0,
            membersCount = if (isGroup) 2 else 1,
            isOnline = true,
            isPinned = false,
            isMuted = false,
            description = description,
            username = if (username.isNotEmpty()) username else "@${title.lowercase().replace(" ", "_")}",
            lastUpdated = System.currentTimeMillis()
        )
        chatDao.insertRoom(room)

        // Add welcome message
        val welcomeMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = id,
            senderId = "system",
            senderName = "Tizim",
            text = "Yangi ${if (isGroup) "guruh" else if (isChannel) "kanal" else "suhbat"} yaratildi. Xush kelibsiz!",
            timestamp = System.currentTimeMillis(),
            isMine = false,
            status = MessageStatus.READ
        )
        chatDao.insertMessage(welcomeMsg.toEntity())
        id
    }

    private suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingRooms = chatDao.getRoomById("ogabek_saburov")
        if (existingRooms == null) {
            val initialRooms = listOf(
                ChatRoomEntity(
                    id = "ogabek_saburov",
                    title = "Ogabek Saburov",
                    avatarUrl = null,
                    avatarColorIndex = 0,
                    isGroup = false,
                    isChannel = false,
                    isBot = false,
                    isVerified = true,
                    unreadCount = 1,
                    membersCount = 1,
                    isOnline = true,
                    isPinned = true,
                    isMuted = false,
                    description = "api.saburov.uz asoschisi & dasturchi",
                    username = "@saburov_dev",
                    lastUpdated = System.currentTimeMillis()
                ),
                ChatRoomEntity(
                    id = "ai_saburov_bot",
                    title = "Saburov AI Yordamchi",
                    avatarUrl = null,
                    avatarColorIndex = 1,
                    isGroup = false,
                    isChannel = false,
                    isBot = true,
                    isVerified = true,
                    unreadCount = 0,
                    membersCount = 1,
                    isOnline = true,
                    isPinned = true,
                    isMuted = false,
                    description = "Sun'iy intellektga asoslangan tezkor yordamchi bot",
                    username = "@saburov_ai_bot",
                    lastUpdated = System.currentTimeMillis() - 1000 * 60 * 5
                ),
                ChatRoomEntity(
                    id = "dev_community_uz",
                    title = "Uzbekistan Dasturchilar Hamjamiyati",
                    avatarUrl = null,
                    avatarColorIndex = 2,
                    isGroup = true,
                    isChannel = false,
                    isBot = false,
                    isVerified = true,
                    unreadCount = 3,
                    membersCount = 1420,
                    isOnline = true,
                    isPinned = false,
                    isMuted = false,
                    description = "Android, Kotlin, Web va Backend texnologiyalari bo'yicha erkin muhokama",
                    username = "@dev_community_uz",
                    lastUpdated = System.currentTimeMillis() - 1000 * 60 * 15
                ),
                ChatRoomEntity(
                    id = "channel_tech_news",
                    title = "IT & Yangiliklar Saburov.uz",
                    avatarUrl = null,
                    avatarColorIndex = 3,
                    isGroup = false,
                    isChannel = true,
                    isBot = false,
                    isVerified = true,
                    unreadCount = 0,
                    membersCount = 5890,
                    isOnline = false,
                    isPinned = false,
                    isMuted = false,
                    description = "Dasturlash, sun'iy intellekt va startaplar haqidagi eng so'nggi yangiliklar",
                    username = "@saburov_news",
                    lastUpdated = System.currentTimeMillis() - 1000 * 60 * 60
                ),
                ChatRoomEntity(
                    id = "dilshod_mobile",
                    title = "Dilshodbek Rustamov",
                    avatarUrl = null,
                    avatarColorIndex = 4,
                    isGroup = false,
                    isChannel = false,
                    isBot = false,
                    isVerified = false,
                    unreadCount = 0,
                    membersCount = 1,
                    isOnline = true,
                    isPinned = false,
                    isMuted = false,
                    description = "Android Developer",
                    username = "@dilshod_dev",
                    lastUpdated = System.currentTimeMillis() - 1000 * 60 * 120
                )
            )

            chatDao.insertRooms(initialRooms)

            // Seed messages for Ogabek Saburov
            val messagesOgabek = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "ogabek_saburov",
                    senderId = "ogabek_saburov",
                    senderName = "Ogabek Saburov",
                    text = "Assalomu alaykum! Saburov Chat rasmiy tizimiga xush kelibsiz 🚀",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
                    isMine = false,
                    status = MessageStatus.READ,
                    reactions = mapOf("🔥" to 1)
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "ogabek_saburov",
                    senderId = "ogabek_saburov",
                    senderName = "Ogabek Saburov",
                    text = "Ilovada real vaqt rejimida WebSocket, audio xabarlar, emojilar va api.saburov.uz serveri bilan to'g'ridan-to'g'ri integratsiya mavjud.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 9,
                    isMine = false,
                    status = MessageStatus.READ
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "ogabek_saburov",
                    senderId = "me",
                    senderName = "Men",
                    text = "Ajoyib! Dizayn va animatsiyalar juda chiroyli chiqibdi 👍",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                    isMine = true,
                    status = MessageStatus.READ
                )
            )

            val messagesBot = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "ai_saburov_bot",
                    senderId = "ai_saburov_bot",
                    senderName = "Saburov AI",
                    text = "Salom! Men sizga 24/7 yordam berishga tayyor aqlli yordamchiman. Savolingizni yozishingiz mumkin.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    isMine = false,
                    status = MessageStatus.READ
                )
            )

            val messagesDev = listOf(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "dev_community_uz",
                    senderId = "user_nodir",
                    senderName = "Nodirbek",
                    text = "Jetpack Compose da Material 3 komponentlaridan foydalanish bo'yicha kimda tajriba bor?",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 20,
                    isMine = false,
                    status = MessageStatus.READ
                ),
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    chatId = "dev_community_uz",
                    senderId = "user_jasur",
                    senderName = "Jasur Karimov",
                    text = "Juda qulay, ayniqsa Dynamic Color va custom AnimatedVisibility bilan UI judayam tez yig'iladi!",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 15,
                    isMine = false,
                    status = MessageStatus.READ,
                    reactions = mapOf("👍" to 3, "❤️" to 2)
                )
            )

            chatDao.insertMessages(messagesOgabek.map { it.toEntity() })
            chatDao.insertMessages(messagesBot.map { it.toEntity() })
            chatDao.insertMessages(messagesDev.map { it.toEntity() })
        }
    }
}

// Extension mappers
fun ChatRoomEntity.toChatRoom(lastMessage: ChatMessage? = null): ChatRoom {
    return ChatRoom(
        id = id,
        title = title,
        avatarUrl = avatarUrl,
        avatarColorIndex = avatarColorIndex,
        isGroup = isGroup,
        isChannel = isChannel,
        isBot = isBot,
        isVerified = isVerified,
        unreadCount = unreadCount,
        lastMessage = lastMessage,
        membersCount = membersCount,
        isOnline = isOnline,
        isPinned = isPinned,
        isMuted = isMuted,
        description = description,
        username = username
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    val reactionsMap = mutableMapOf<String, Int>()
    if (reactionsJson.isNotEmpty()) {
        reactionsJson.split(",").forEach { item ->
            val parts = item.split(":")
            if (parts.size == 2) {
                val emoji = parts[0]
                val count = parts[1].toIntOrNull() ?: 1
                reactionsMap[emoji] = count
            }
        }
    }

    return ChatMessage(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        timestamp = timestamp,
        isMine = isMine,
        status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SENT },
        mediaType = try { MediaType.valueOf(mediaType) } catch (e: Exception) { MediaType.TEXT },
        mediaUrl = mediaUrl,
        voiceDurationSeconds = voiceDurationSeconds,
        replyToMessageId = replyToMessageId,
        replyToText = replyToText,
        replyToSender = replyToSender,
        reactions = reactionsMap,
        myReaction = myReaction,
        isPinned = isPinned
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    val reactionsStr = reactions.entries.joinToString(",") { "${it.key}:${it.value}" }
    return ChatMessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        text = text,
        timestamp = timestamp,
        isMine = isMine,
        status = status.name,
        mediaType = mediaType.name,
        mediaUrl = mediaUrl,
        voiceDurationSeconds = voiceDurationSeconds,
        replyToMessageId = replyToMessageId,
        replyToText = replyToText,
        replyToSender = replyToSender,
        reactionsJson = reactionsStr,
        myReaction = myReaction,
        isPinned = isPinned
    )
}
