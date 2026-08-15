package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioVoicePlayer(
    durationSeconds: Int,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAnim"
    )

    val barHeights = remember { listOf(0.4f, 0.8f, 0.6f, 1.0f, 0.5f, 0.9f, 0.7f, 0.3f, 0.85f, 0.45f, 0.75f, 0.5f) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isMine) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMine) Color.White else MaterialTheme.colorScheme.primary)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "To'xtatish" else "Ijro etish",
                tint = if (isMine) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Waveform bars
        Row(
            modifier = Modifier.height(24.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            barHeights.forEachIndexed { index, baseHeight ->
                val dynamicFactor = if (isPlaying) {
                    val offset = (index % 3) * 0.2f
                    (waveAnim + offset).coerceIn(0.2f, 1f)
                } else 0.6f

                val currentHeight = 24.dp * (baseHeight * dynamicFactor)
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(currentHeight.coerceAtLeast(4.dp))
                        .clip(CircleShape)
                        .background(
                            if (isMine) Color.White.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                )
            }
        }

        Text(
            text = "0:${durationSeconds.toString().padStart(2, '0')}",
            color = if (isMine) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
