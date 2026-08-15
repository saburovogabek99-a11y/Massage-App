package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageStatus
import com.example.ui.theme.DarkMessageReceived
import com.example.ui.theme.DarkMessageSent
import com.example.ui.theme.ElegantDarkTextSecondary
import com.example.ui.theme.ElegantMessageReceived
import com.example.ui.theme.ElegantMessageSent
import com.example.ui.theme.ElegantOnMessageReceived
import com.example.ui.theme.ElegantOnMessageSent
import com.example.ui.theme.ElegantPrimary
import com.example.ui.theme.LightMessageReceived
import com.example.ui.theme.LightMessageSent
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isDarkTheme: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMine = message.isMine
    val bubbleShape = if (isMine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        // Pinned indicator
        if (message.isPinned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Qadalgan",
                    tint = PrimaryCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Qadalgan xabar",
                    fontSize = 10.sp,
                    color = PrimaryCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        val bubbleBg = if (isMine) {
            if (isDarkTheme) ElegantMessageSent else LightMessageSent
        } else {
            if (isDarkTheme) ElegantMessageReceived else LightMessageReceived
        }
        val textColor = if (isMine) {
            if (isDarkTheme) ElegantOnMessageSent else Color.White
        } else {
            if (isDarkTheme) ElegantOnMessageReceived else LightTextPrimary
        }
        val metaColor = if (isMine) {
            if (isDarkTheme) ElegantOnMessageSent.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f)
        } else {
            if (isDarkTheme) ElegantDarkTextSecondary else LightTextSecondary
        }

        Box(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 320.dp)
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .background(bubbleBg)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                // Sender name in group
                if (!isMine && message.senderName.isNotBlank() && message.senderName != "Men") {
                    Text(
                        text = message.senderName,
                        color = if (isDarkTheme) ElegantPrimary else PrimaryBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Reply preview quote
                if (!message.replyToText.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMine) textColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(28.dp)
                                .background(if (isMine) textColor else PrimaryBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = message.replyToSender ?: "Xabar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMine) textColor else PrimaryBlue
                            )
                            Text(
                                text = message.replyToText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                color = if (isMine) textColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Message Body
                when (message.mediaType) {
                    MediaType.VOICE -> {
                        AudioVoicePlayer(
                            durationSeconds = message.voiceDurationSeconds.coerceAtLeast(3),
                            isMine = isMine
                        )
                    }
                    MediaType.IMAGE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🖼️ Rasm fayli", color = Color.White)
                        }
                        if (message.text.isNotBlank() && message.text != "📷 Rasm") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.text,
                                color = textColor,
                                fontSize = 14.5.sp
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Timestamp and Delivery Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = metaColor
                    )

                    if (isMine) {
                        when (message.status) {
                            MessageStatus.SENDING -> Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Yuborilmoqda",
                                tint = metaColor,
                                modifier = Modifier.size(13.dp)
                            )
                            MessageStatus.SENT -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Yuborildi",
                                tint = metaColor,
                                modifier = Modifier.size(13.dp)
                            )
                            MessageStatus.DELIVERED -> Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Yetkazildi",
                                tint = metaColor,
                                modifier = Modifier.size(14.dp)
                            )
                            MessageStatus.READ -> Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "O'qildi",
                                tint = if (isDarkTheme) textColor else PrimaryCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reaction chips below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = if (isMine) 0.dp else 4.dp, end = if (isMine) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { (emoji, count) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (message.myReaction == emoji) PrimaryBlue.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (message.myReaction == emoji) PrimaryBlue else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .combinedClickable(
                                onClick = { onReactionClick(emoji) },
                                onLongClick = {}
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$emoji $count",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
