package com.klim.trossage_android.ui.chat.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klim.trossage_android.domain.model.Message
import androidx.compose.foundation.layout.navigationBarsPadding


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: ChatDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.companionName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                MessageInput(
                    text = uiState.messageText,
                    onTextChanged = viewModel::onMessageTextChanged,
                    onSendClick = viewModel::sendMessage,
                    enabled = !uiState.isSending,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true
                ) {
                    items(uiState.messages.reversed()) { message ->
                        MessageItem(message = message)
                    }

                    if (uiState.companionIsTyping) {
                        item {
                            Text(
                                text = "${uiState.companionName} печатает...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (uiState.hasMore && !uiState.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.loadMoreMessages() }) {
                                    Text("Загрузить ещё")
                                }
                            }
                        }
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
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Сообщение") },
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSendClick,
            enabled = enabled && text.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
        }
    }
}


@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (message.isMine) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (!message.isMine) {
                    Text(
                        text = message.senderDisplayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = com.klim.trossage_android.data.util.DateUtils.formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
