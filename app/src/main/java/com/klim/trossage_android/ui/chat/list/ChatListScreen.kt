package com.klim.trossage_android.ui.chat.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.klim.trossage_android.domain.model.Chat
import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel,
    onChatClick: (Chat) -> Unit,
    onSettingsClick: () -> Unit,
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var refreshing by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
            scope.launch {
                try {
                    viewModel.refresh()
                } finally {
                    refreshing = false
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чаты") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearchDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Новый чат")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
                state = listState
            ) {
                itemsIndexed(uiState.chats) { index, chat ->
                    ChatListItem(
                        chat = chat,
                        onClick = { onChatClick(chat) }
                    )
                    HorizontalDivider()

                    if (index == uiState.chats.size - 1 && uiState.hasMore && !uiState.isLoading) {
                        LaunchedEffect(Unit) {
                            viewModel.loadChats()
                        }
                    }
                }

                if (uiState.isLoading && uiState.chats.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (uiState.isLoading && uiState.chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.chats.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет чатов")
                }
            }
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(error)
            }
        }
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = {
                showSearchDialog = false
                searchQuery = ""
                viewModel.searchUsers("")
            },
            title = { Text("Найти пользователя") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchUsers(it)
                        },
                        label = { Text("Логин пользователя") },
                        placeholder = { Text("Начните вводить...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (searchState.isLoading && searchState.users.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (searchState.users.isEmpty() && searchQuery.isNotBlank() && !searchState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Пользователи не найдены")
                        }
                    } else if (searchState.users.isNotEmpty()) {
                        val searchListState = rememberLazyListState()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            state = searchListState
                        ) {
                            itemsIndexed(searchState.users) { index, user ->
                                UserSearchItem(
                                    user = user,
                                    onClick = {
                                        viewModel.createChat(user.userId.toInt()) { chatId, companionName ->
                                            navController.navigate(
                                                Screen.ChatDetail.createRoute(chatId, companionName)
                                            )
                                        }
                                        showSearchDialog = false
                                        searchQuery = ""
                                        viewModel.searchUsers("")
                                    }
                                )

                                if (index == searchState.users.size - 1 && searchState.hasMore) {
                                    LaunchedEffect(Unit) {
                                        viewModel.loadMoreSearchResults()
                                    }
                                }
                            }

                            if (searchState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Введите логин для поиска", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    searchState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSearchDialog = false
                    searchQuery = ""
                    viewModel.searchUsers("")
                }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
fun UserSearchItem(
    user: User,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.companionDisplayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = chat.lastMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}
