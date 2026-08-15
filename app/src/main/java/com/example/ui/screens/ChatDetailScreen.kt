package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.ui.components.AvatarView
import com.example.ui.components.MessageBubble
import com.example.ui.components.QuickEmojis
import com.example.ui.components.ReactionsPickerBar
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryCyan
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    room: ChatRoom,
    messages: List<ChatMessage>,
    typingStatus: String?,
    replyingMessage: ChatMessage?,
    isRecordingVoice: Boolean,
    voiceSeconds: Int,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStartVoiceRecording: () -> Unit,
    onFinishVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onSendSampleImage: (String, String) -> Unit,
    onSetReplyingTo: (ChatMessage?) -> Unit,
    onToggleReaction: (ChatMessage, String) -> Unit,
    onTogglePinMessage: (ChatMessage) -> Unit,
    onDeleteMessage: (ChatMessage) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var selectedMessageForMenu by remember { mutableStateOf<ChatMessage?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf<String?>(null) } // "audio" or "video"
    var showMenuDropdown by remember { mutableStateOf(false) }
    var showEmojiQuickBar by remember { mutableStateOf(false) }

    // Scroll to bottom when messages list size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val pinnedMessage = remember(messages) { messages.lastOrNull { it.isPinned } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AvatarView(
                            name = room.title,
                            size = 40.dp,
                            colorIndex = room.avatarColorIndex,
                            isOnline = room.isOnline,
                            isGroup = room.isGroup,
                            isChannel = room.isChannel,
                            isBot = room.isBot
                        )

                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = room.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (room.isVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Tasdiqlangan",
                                        tint = PrimaryCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            val subtitle = when {
                                !typingStatus.isNullOrEmpty() -> typingStatus
                                room.isBot -> "Bot • api.saburov.uz"
                                room.isChannel -> "${room.membersCount} obunachi"
                                room.isGroup -> "${room.membersCount} a'zo"
                                room.isOnline -> "onlayn"
                                else -> "oxirgi marta yaqinda"
                            }

                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = if (room.isOnline || !typingStatus.isNullOrEmpty()) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCallDialog = "audio" }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Qo'ng'iroq", tint = PrimaryCyan)
                    }
                    IconButton(onClick = { showCallDialog = "video" }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video qo'ng'iroq", tint = PrimaryCyan)
                    }
                    IconButton(onClick = { showMenuDropdown = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Ko'proq")
                    }

                    DropdownMenu(
                        expanded = showMenuDropdown,
                        onDismissRequest = { showMenuDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil ma'lumotlari") },
                            onClick = {
                                showMenuDropdown = false
                                Toast.makeText(context, "${room.title} (@${room.username})", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("api.saburov.uz server orqali tekshirish") },
                            onClick = {
                                showMenuDropdown = false
                                Toast.makeText(context, "Suhbat api.saburov.uz xavfsiz kanali bilan himoyalangan", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Pinned Message Banner
                if (pinnedMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                val idx = messages.indexOf(pinnedMessage)
                                if (idx >= 0) {
                                    scope.launch { listState.animateScrollToItem(idx) }
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Qadalgan",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Qadalgan xabar (${pinnedMessage.senderName})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                            Text(
                                text = pinnedMessage.text,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Reply Preview Bar
                AnimatedVisibility(visible = replyingMessage != null) {
                    if (replyingMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(32.dp)
                                    .background(PrimaryBlue, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Javob berilmoqda: ${replyingMessage.senderName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = replyingMessage.text,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onSetReplyingTo(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Bekor qilish",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Quick Emojis Bar
                AnimatedVisibility(visible = showEmojiQuickBar) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("😊", "👍", "❤️", "🔥", "🚀", "😂", "🎉", "🤝").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clickable {
                                        inputText += emoji
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Main Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attach button
                    IconButton(
                        onClick = { showAttachmentSheet = true },
                        modifier = Modifier.testTag("attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Biriktirish",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Emoji toggle
                    IconButton(
                        onClick = { showEmojiQuickBar = !showEmojiQuickBar },
                        modifier = Modifier.testTag("emoji_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emojilar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Recording mode vs Text field
                    if (isRecordingVoice) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Yozilmoqda... 0:${voiceSeconds.toString().padStart(2, '0')}",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            TextButton(onClick = onCancelVoiceRecording) {
                                Text("Bekor qilish", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Stop/Send voice button
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .clickable { onFinishVoiceRecording() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Yuborish",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Regular text input
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Xabar yozing...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Action button: Send if text is present, or Mic if empty
                        if (inputText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        val toSend = inputText
                                        inputText = ""
                                        onSendMessage(toSend)
                                    }
                                    .testTag("send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Yuborish",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onStartVoiceRecording() }
                                    .testTag("voice_record_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Ovoz yozish",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                // Welcome header card inside chat
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarView(
                            name = room.title,
                            size = 64.dp,
                            colorIndex = room.avatarColorIndex,
                            isOnline = room.isOnline,
                            isGroup = room.isGroup,
                            isChannel = room.isChannel,
                            isBot = room.isBot
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = room.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = room.description.ifEmpty { room.username },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔒 Xabarlar api.saburov.uz shifrlangan kanali orqali uzatilmoqda",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isDarkTheme = isDarkTheme,
                        onLongClick = { selectedMessageForMenu = msg },
                        onClick = { selectedMessageForMenu = msg },
                        onReactionClick = { emoji -> onToggleReaction(msg, emoji) },
                        onReplyClick = { onSetReplyingTo(msg) }
                    )
                }
            }

            // Floating Reactions bar when a message is selected
            if (selectedMessageForMenu != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable { selectedMessageForMenu = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        ReactionsPickerBar(
                            onSelectEmoji = { emoji ->
                                selectedMessageForMenu?.let { onToggleReaction(it, emoji) }
                                selectedMessageForMenu = null
                            }
                        )

                        // Message actions box
                        Box(
                            modifier = Modifier
                                .shadow(10.dp, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = {
                                        onSetReplyingTo(selectedMessageForMenu)
                                        selectedMessageForMenu = null
                                    }
                                ) {
                                    Icon(Icons.Default.Reply, contentDescription = "Javob berish", tint = PrimaryBlue)
                                }

                                IconButton(
                                    onClick = {
                                        selectedMessageForMenu?.let {
                                            clipboardManager.setText(AnnotatedString(it.text))
                                            Toast.makeText(context, "Nusxa olindi", Toast.LENGTH_SHORT).show()
                                        }
                                        selectedMessageForMenu = null
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Nusxalash", tint = MaterialTheme.colorScheme.onSurface)
                                }

                                IconButton(
                                    onClick = {
                                        selectedMessageForMenu?.let { onTogglePinMessage(it) }
                                        selectedMessageForMenu = null
                                    }
                                ) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Qadash", tint = PrimaryCyan)
                                }

                                IconButton(
                                    onClick = {
                                        selectedMessageForMenu?.let { onDeleteMessage(it) }
                                        selectedMessageForMenu = null
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "O'chirish", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachments bottom sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Fayl yoki media biriktirish",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            onSendSampleImage("https://images.unsplash.com/photo-1518770660439-4636190af475", "Saburov Chat orqali yuborilgan rasm")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = "Galereya", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Galereya", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            onSendMessage("📍 Toshkent, O'zbekiston (41.311081, 69.240562)")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Joylashuv", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Joylashuv", fontSize = 12.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            showAttachmentSheet = false
                            onStartVoiceRecording()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Ovoz", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ovozli xabar", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Simulated Call Dialog
    if (showCallDialog != null) {
        val isVideo = showCallDialog == "video"
        AlertDialog(
            onDismissRequest = { showCallDialog = null },
            title = {
                Text(
                    text = if (isVideo) "HD Video Qo'ng'iroq" else "Ovozli Qo'ng'iroq",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AvatarView(
                        name = room.title,
                        size = 72.dp,
                        colorIndex = room.avatarColorIndex,
                        isOnline = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = room.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "api.saburov.uz xavfsiz WebRTC serveri orqali ulanmoqda...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallDialog = null }) {
                    Text("Yakunlash", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
