package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnlineGreen

val AvatarGradients = listOf(
    listOf(Color(0xFFD0BCFF), Color(0xFF9A82DB)), // Elegant Lilac
    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)), // Royal Violet
    listOf(Color(0xFF6750A4), Color(0xFF4F378B)), // Deep Amethyst
    listOf(Color(0xFFEFB8C8), Color(0xFFB58392)), // Rose Coral
    listOf(Color(0xFF4FD1C5), Color(0xFF319795)), // Soft Teal
    listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))  // Modern Blue
)

@Composable
fun AvatarView(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    colorIndex: Int = 0,
    isOnline: Boolean = false,
    isGroup: Boolean = false,
    isChannel: Boolean = false,
    isBot: Boolean = false
) {
    val gradientColors = AvatarGradients[colorIndex.coerceIn(0, AvatarGradients.size - 1)]
    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "?" }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isBot -> Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Bot",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
                isChannel -> Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Kanal",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
                isGroup -> Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Guruh",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
                else -> Text(
                    text = initials,
                    color = Color.White,
                    fontSize = (size.value * 0.38f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isOnline && !isGroup && !isChannel) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .background(OnlineGreen, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
