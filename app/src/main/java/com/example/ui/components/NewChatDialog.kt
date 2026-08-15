package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue

enum class ChatTypeCreation {
    DIRECT,
    GROUP,
    CHANNEL
}

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onCreateChat: (title: String, description: String, isGroup: Boolean, isChannel: Boolean, username: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chatType by remember { mutableStateOf(ChatTypeCreation.DIRECT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yangi Suhbat Yaratish",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = chatType == ChatTypeCreation.DIRECT,
                        onClick = { chatType = ChatTypeCreation.DIRECT },
                        label = { Text("Shaxsiy") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    FilterChip(
                        selected = chatType == ChatTypeCreation.GROUP,
                        onClick = { chatType = ChatTypeCreation.GROUP },
                        label = { Text("Guruh") },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) }
                    )
                    FilterChip(
                        selected = chatType == ChatTypeCreation.CHANNEL,
                        onClick = { chatType = ChatTypeCreation.CHANNEL },
                        label = { Text("Kanal") },
                        leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null) }
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (chatType) {
                                ChatTypeCreation.DIRECT -> "Foydalanuvchi ismi"
                                ChatTypeCreation.GROUP -> "Guruh nomi"
                                ChatTypeCreation.CHANNEL -> "Kanal nomi"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Foydalanuvchi nomi (@username)") },
                    singleLine = true,
                    placeholder = { Text("@foydalanuvchi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Tavsif (Bio)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateChat(
                            title.trim(),
                            description.trim(),
                            chatType == ChatTypeCreation.GROUP,
                            chatType == ChatTypeCreation.CHANNEL,
                            username.trim()
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Yaratish")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
