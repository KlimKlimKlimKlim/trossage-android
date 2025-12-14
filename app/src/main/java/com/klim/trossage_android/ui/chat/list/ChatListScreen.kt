package com.klim.trossage_android.ui.chat.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onChatClick: (Chat) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var showSearchDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чаты") },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Новый чат")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.chats, key = { it.chatId }) { chat ->
                    ChatItem(
                        chat = chat,
                        onClick = { onChatClick(chat) },
                        onDelete = { viewModel.deleteChat(chat.chatId) }
                    )
                    Divider()
                }

                if (uiState.isLoading && uiState.chats.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            LaunchedEffect(listState) {
                snapshotFlow {
                    listState.firstVisibleItemIndex == 0 &&
                            listState.firstVisibleItemScrollOffset == 0
                }.collect { isAtTop ->
                    if (isAtTop) {
                    }
                }
            }

            LaunchedEffect(listState) {
                snapshotFlow {
                    val lastIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastIndex >= uiState.chats.size - 3
                }.collect { shouldLoad ->
                    if (shouldLoad && !uiState.isLoading && uiState.hasMore) {
                        viewModel.loadChats()
                    }
                }
            }

            if (uiState.isLoading && uiState.chats.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }

    if (showSearchDialog) {
        SearchUserDialog(
            searchQuery = uiState.searchQuery,
            searchResults = uiState.searchResults,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onUserClick = { user ->
                showSearchDialog = false
                viewModel.openChatWithUser(user.userId) { chatId ->
                    onChatClick(chatId)
                }
            },
            onDismiss = { showSearchDialog = false }
        )
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.companionDisplayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (!chat.isRead) FontWeight.Bold else FontWeight.Normal
            )
            chat.lastMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatTimestamp(chat.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!chat.isRead) {
                Surface(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserDialog(
    searchQuery: String,
    searchResults: List<User>,
    onQueryChange: (String) -> Unit,
    onUserClick: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый чат") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    label = { Text("Введите логин") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(searchResults) { user ->
                        ListItem(
                            headlineContent = { Text(user.displayName) },
                            supportingContent = { Text(user.username) },
                            modifier = Modifier.clickable { onUserClick(user) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Только что"
        diff < 3600_000 -> "${diff / 60_000} мин"
        diff < 86400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
    }
}