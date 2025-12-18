package com.klim.trossage_android.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoadingData) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Карточка профиля
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Профиль",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Логин: ${state.currentLogin}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Имя: ${state.currentDisplayName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Настройки аккаунта
                    Text(
                        text = "Настройки аккаунта",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = viewModel::showDisplayNameDialog,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Изменить отображаемое имя")
                    }

                    Button(
                        onClick = viewModel::showPasswordDialog,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Сменить пароль")
                    }

                    HorizontalDivider()

                    // Управление сессиями
                    Text(
                        text = "Управление сессиями",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { viewModel.logout(onLoggedOut) },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Выйти с этого устройства")
                    }

                    OutlinedButton(
                        onClick = viewModel::logoutAllDevices,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Выйти со всех устройств")
                    }

                    HorizontalDivider()

                    Text(
                        text = "Опасная зона",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = viewModel::showDeleteAccountDialog,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Удалить аккаунт навсегда")
                    }

                    state.error?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = viewModel::clearMessages,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    state.info?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = viewModel::clearMessages,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Закрыть",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showDisplayNameDialog) {
        ChangeDisplayNameDialog(
            currentName = state.currentDisplayName,
            newName = state.newDisplayName,
            error = state.error,
            isLoading = state.isLoading,
            onNewNameChange = viewModel::onDisplayNameChanged,
            onConfirm = viewModel::confirmDisplayNameChange,
            onDismiss = viewModel::dismissDisplayNameDialog
        )
    }

    if (state.showPasswordDialog) {
        ChangePasswordDialog(
            oldPassword = state.oldPassword,
            newPassword = state.newPassword,
            newPasswordConfirm = state.newPasswordConfirm,
            error = state.error,
            isLoading = state.isLoading,
            onOldPasswordChange = viewModel::onOldPasswordChanged,
            onNewPasswordChange = viewModel::onNewPasswordChanged,
            onNewPasswordConfirmChange = viewModel::onNewPasswordConfirmChanged,
            onConfirm = viewModel::confirmPasswordChange,
            onDismiss = viewModel::dismissPasswordDialog
        )
    }

    if (state.showDeleteAccountDialog) {
        DeleteAccountDialog(
            password = state.deletePassword,
            error = state.error,
            isLoading = state.isLoading,
            onPasswordChange = viewModel::onDeletePasswordChanged,
            onConfirm = { viewModel.confirmDeleteAccount(onLoggedOut) },
            onDismiss = viewModel::dismissDeleteAccountDialog
        )
    }
}

@Composable
fun ChangeDisplayNameDialog(
    currentName: String,
    newName: String,
    error: String?,
    isLoading: Boolean,
    onNewNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Изменить имя") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Текущее: $currentName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = onNewNameChange,
                    label = { Text("Новое имя") },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("1–20 символов") }
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Изменить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    oldPassword: String,
    newPassword: String,
    newPasswordConfirm: String,
    error: String?,
    isLoading: Boolean,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onNewPasswordConfirmChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Смена пароля") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Сервер автоматически разлогинит все устройства при смене пароля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Текущий пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("8–63 символа") }
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("Новый пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("8–63 символа") }
                )

                OutlinedTextField(
                    value = newPasswordConfirm,
                    onValueChange = onNewPasswordConfirmChange,
                    label = { Text("Подтвердите новый пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сменить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    password: String,
    error: String?,
    isLoading: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Удалить аккаунт") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "⚠️ Это действие необратимо!",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Все ваши данные будут безвозвратно удалены:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "• Профиль и настройки\n• Все сообщения\n• История чатов",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Введите пароль для подтверждения") },
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("8–63 символа") }
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Удалить навсегда")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}
