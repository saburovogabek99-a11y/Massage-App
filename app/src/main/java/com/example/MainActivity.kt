package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ApiSettingsSheet
import com.example.ui.components.NewChatDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.theme.SaburovChatTheme
import com.example.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val filteredRooms by viewModel.filteredRooms.collectAsStateWithLifecycle()
            val allRooms by viewModel.rawRooms.collectAsStateWithLifecycle()
            val apiStatus by viewModel.apiStatus.collectAsStateWithLifecycle()
            val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
            val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
            val activeChat by viewModel.activeChat.collectAsStateWithLifecycle()
            val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
            val typingStatus by viewModel.typingStatus.collectAsStateWithLifecycle()
            val replyingMessage by viewModel.replyingMessage.collectAsStateWithLifecycle()
            val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
            val voiceSeconds by viewModel.voiceSeconds.collectAsStateWithLifecycle()

            var showApiSettingsSheet by remember { mutableStateOf(false) }
            var showNewChatDialog by remember { mutableStateOf(false) }
            var showProfileDialog by remember { mutableStateOf(false) }

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            SaburovChatTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (activeChat != null) {
                        BackHandler {
                            viewModel.closeChat()
                        }

                        ChatDetailScreen(
                            room = activeChat!!,
                            messages = currentMessages,
                            typingStatus = typingStatus[activeChat!!.id],
                            replyingMessage = replyingMessage,
                            isRecordingVoice = isRecordingVoice,
                            voiceSeconds = voiceSeconds,
                            isDarkTheme = isDarkTheme,
                            onBack = { viewModel.closeChat() },
                            onSendMessage = { viewModel.sendMessage(it) },
                            onStartVoiceRecording = { viewModel.startVoiceRecording() },
                            onFinishVoiceRecording = { viewModel.finishVoiceRecording() },
                            onCancelVoiceRecording = { viewModel.cancelVoiceRecording() },
                            onSendSampleImage = { url, caption -> viewModel.sendSampleImage(url, caption) },
                            onSetReplyingTo = { viewModel.setReplyingTo(it) },
                            onToggleReaction = { msg, emoji -> viewModel.toggleReaction(msg, emoji) },
                            onTogglePinMessage = { viewModel.togglePinMessage(it) },
                            onDeleteMessage = { viewModel.deleteMessage(it) }
                        )
                    } else {
                        ChatListScreen(
                            rooms = filteredRooms,
                            allRooms = allRooms,
                            apiStatus = apiStatus,
                            selectedFilter = selectedFilter,
                            searchQuery = searchQuery,
                            typingMap = typingStatus,
                            isDarkTheme = isDarkTheme,
                            onFilterChange = { viewModel.setFilter(it) },
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onChatClick = { viewModel.openChat(it) },
                            onPinChatClick = { viewModel.togglePinRoom(it) },
                            onOpenNewChat = { showNewChatDialog = true },
                            onOpenApiSettings = { showApiSettingsSheet = true },
                            onOpenProfile = { showProfileDialog = true },
                            onToggleTheme = { viewModel.toggleTheme() }
                        )
                    }

                    // Dialogs & Sheets
                    if (showApiSettingsSheet) {
                        ApiSettingsSheet(
                            apiStatus = apiStatus,
                            sheetState = sheetState,
                            onDismiss = { showApiSettingsSheet = false },
                            onTestPing = { url -> viewModel.refreshApiConnection(url) }
                        )
                    }

                    if (showNewChatDialog) {
                        NewChatDialog(
                            onDismiss = { showNewChatDialog = false },
                            onCreateChat = { title, desc, isGroup, isChannel, username ->
                                viewModel.createNewChat(title, desc, isGroup, isChannel, username)
                            }
                        )
                    }

                    if (showProfileDialog) {
                        UserProfileDialog(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleTheme() },
                            onOpenApiSettings = { showApiSettingsSheet = true },
                            onDismiss = { showProfileDialog = false }
                        )
                    }
                }
            }
        }
    }
}
