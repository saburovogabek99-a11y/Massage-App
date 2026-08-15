package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatRoom
import com.example.data.model.MediaType
import com.example.data.model.ServerApiStatus
import com.example.ui.components.AvatarView
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryCyan
import com.example.ui.viewmodel.ChatFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    rooms: List<ChatRoom>,
    allRooms: List<ChatRoom>,
    apiStatus: ServerApiStatus,
    selectedFilter: ChatFilter,
    searchQuery: String,
    typingMap: Map<String, String>,
    isDarkTheme: Boolean,
    onFilterChange: (ChatFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onChatClick: (ChatRoom) -> Unit,
    onPinChatClick: (ChatRoom) -> Unit,
    onOpenNewChat: () -> Unit,
    onOpenApiSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onToggleTheme: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .shadow(2.dp)
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AvatarView(
                                name = "Saburov Chat",
                                size = 36.dp,
                                colorIndex = 0
                            )
                            Column {
                                Text(
                                    text = "Saburov Chat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Server Status Subtitle
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { onOpenApiSettings() }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (apiStatus.isConnected) OnlineGreen else Color.Gray)
                                    )
                                    Text(
                                        text = "api.saburov.uz • ${apiStatus.pingMs}ms",
                                        fontSize = 11.sp,
                                        color = if (apiStatus.isConnected) OnlineGreen else Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) onSearchChange("")
                            },
                            modifier = Modifier.testTag("search_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Qidirish",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onOpenApiSettings,
                            modifier = Modifier.testTag("api_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Server Sozlamalari",
                                tint = PrimaryCyan
                            )
                        }

                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Mavzu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = onOpenProfile,
                            modifier = Modifier.testTag("profile_button")
                        ) {
                            AvatarView(
                                name = "User",
                                size = 32.dp,
                                colorIndex = 0
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Search field if active
                AnimatedVisibility(visible = isSearchActive) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Suhbatlar yoki xabarlarni qidirish...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_text_input"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Tozalash")
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Filter tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ChatFilter.values()) { filter ->
                        val isSelected = filter == selectedFilter
                        InputChip(
                            selected = isSelected,
                            onClick = { onFilterChange(filter) },
                            label = {
                                Text(
                                    text = filter.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewChat,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("new_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Yangi suhbat",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Online Contacts horizontal carousel
            item {
                Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                    Text(
                        text = "FAOL DO'STLAR & KANALLAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        letterSpacing = 0.8.sp
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(allRooms.take(8)) { room ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(62.dp)
                                    .clickable { onChatClick(room) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(PrimaryBlue, PrimaryCyan, Color(0xFF8B5CF6), PrimaryBlue)
                                            )
                                        )
                                        .padding(2.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarView(
                                        name = room.title,
                                        size = 49.dp,
                                        colorIndex = room.avatarColorIndex,
                                        isOnline = room.isOnline,
                                        isGroup = room.isGroup,
                                        isChannel = room.isChannel,
                                        isBot = room.isBot
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = room.title.split(" ").firstOrNull() ?: room.title,
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }
            }

            // Chat list items
            if (rooms.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Suhbatlar topilmadi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Yangi suhbat boshlash uchun + tugmasini bosing",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(rooms, key = { it.id }) { room ->
                    ChatListItem(
                        room = room,
                        typingStatus = typingMap[room.id],
                        onClick = { onChatClick(room) },
                        onPinClick = { onPinChatClick(room) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    room: ChatRoom,
    typingStatus: String?,
    onClick: () -> Unit,
    onPinClick: () -> Unit
) {
    val lastMsg = room.lastMessage
    val formattedTime = if (lastMsg != null) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastMsg.timestamp))
    } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AvatarView(
            name = room.title,
            size = 52.dp,
            colorIndex = room.avatarColorIndex,
            isOnline = room.isOnline,
            isGroup = room.isGroup,
            isChannel = room.isChannel,
            isBot = room.isBot
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = room.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (room.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Qadalgan",
                            tint = PrimaryCyan,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onPinClick() }
                        )
                    }
                    if (formattedTime.isNotEmpty()) {
                        Text(
                            text = formattedTime,
                            fontSize = 11.5.sp,
                            color = if (room.unreadCount > 0) PrimaryCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtitle / Last message / Typing
                if (!typingStatus.isNullOrEmpty()) {
                    Text(
                        text = typingStatus,
                        fontSize = 13.5.sp,
                        color = PrimaryCyan,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else if (lastMsg != null) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (lastMsg.mediaType == MediaType.VOICE) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        } else if (lastMsg.mediaType == MediaType.IMAGE) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = if (lastMsg.isMine) "Siz: ${lastMsg.text}" else lastMsg.text,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = room.description.ifEmpty { room.username },
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Unread Badge
                if (room.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = room.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
