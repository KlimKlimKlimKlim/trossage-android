package com.klim.trossage_android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.domain.repository.AuthRepository
import com.klim.trossage_android.domain.repository.SessionRepository
import com.klim.trossage_android.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val authRepository: AuthRepository,
    private val authPrefs: AuthPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(authPrefs.isDarkMode())
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingData = true)
            userRepository.getMe()
                .onSuccess { user ->
                    _state.value = _state.value.copy(
                        currentDisplayName = user.displayName,
                        currentLogin = user.username,
                        isLoadingData = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoadingData = false,
                        error = "Не удалось загрузить данные: ${e.message}"
                    )
                }
        }
    }

    fun toggleDarkMode() {
        val newValue = !_darkModeEnabled.value
        _darkModeEnabled.value = newValue
        authPrefs.setDarkMode(newValue)
    }

    fun showDisplayNameDialog() {
        _state.value = _state.value.copy(
            showDisplayNameDialog = true,
            newDisplayName = "",
            error = null
        )
    }

    fun dismissDisplayNameDialog() {
        _state.value = _state.value.copy(
            showDisplayNameDialog = false,
            newDisplayName = ""
        )
    }

    fun onDisplayNameChanged(v: String) {
        _state.value = _state.value.copy(newDisplayName = v, error = null)
    }

    fun confirmDisplayNameChange() {
        val name = state.value.newDisplayName.trim()
        if (name.length !in 1..20) {
            _state.value = _state.value.copy(error = "Имя: 1–20 символов")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            userRepository.updateDisplayName(name)
                .onSuccess { u ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        showDisplayNameDialog = false,
                        currentDisplayName = u.displayName,
                        newDisplayName = "",
                        info = "Имя обновлено"
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка обновления имени"
                    )
                }
        }
    }

    fun showPasswordDialog() {
        _state.value = _state.value.copy(
            showPasswordDialog = true,
            oldPassword = "",
            newPassword = "",
            newPasswordConfirm = "",
            error = null
        )
    }

    fun dismissPasswordDialog() {
        _state.value = _state.value.copy(
            showPasswordDialog = false,
            oldPassword = "",
            newPassword = "",
            newPasswordConfirm = ""
        )
    }

    fun onOldPasswordChanged(v: String) {
        _state.value = _state.value.copy(oldPassword = v, error = null, info = null)
    }

    fun onNewPasswordChanged(v: String) {
        _state.value = _state.value.copy(newPassword = v, error = null, info = null)
    }

    fun onNewPasswordConfirmChanged(v: String) {
        _state.value = _state.value.copy(newPasswordConfirm = v, error = null, info = null)
    }

    fun confirmPasswordChange() {
        val old = state.value.oldPassword
        val new = state.value.newPassword
        val confirm = state.value.newPasswordConfirm

        when {
            old.length !in 8..63 -> {
                _state.value = _state.value.copy(error = "Старый пароль: 8–63 символа")
                return
            }
            new.length !in 8..63 -> {
                _state.value = _state.value.copy(error = "Новый пароль: 8–63 символа")
                return
            }
            new != confirm -> {
                _state.value = _state.value.copy(error = "Пароли не совпадают")
                return
            }
            new == old -> {
                _state.value = _state.value.copy(error = "Новый пароль совпадает со старым")
                return
            }
        }

        val currentUser = userRepository.getCurrentUser()
        if (currentUser == null) {
            _state.value = _state.value.copy(error = "Не удалось получить текущего пользователя")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            userRepository.changePassword(old, new)
                .onSuccess {
                    authRepository.login(currentUser.username, new)
                        .onSuccess {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                showPasswordDialog = false,
                                oldPassword = "",
                                newPassword = "",
                                newPasswordConfirm = "",
                                info = "Пароль изменён. Вы переавторизованы."
                            )
                        }
                        .onFailure { e ->
                            _state.value = _state.value.copy(
                                isLoading = false,
                                showPasswordDialog = false,
                                oldPassword = "",
                                newPassword = "",
                                newPasswordConfirm = "",
                                error = "Пароль изменён, но не удалось войти заново: ${e.message}"
                            )
                        }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка смены пароля"
                    )
                }
        }
    }

    fun showDeleteAccountDialog() {
        _state.value = _state.value.copy(
            showDeleteAccountDialog = true,
            deletePassword = "",
            error = null
        )
    }

    fun dismissDeleteAccountDialog() {
        _state.value = _state.value.copy(
            showDeleteAccountDialog = false,
            deletePassword = ""
        )
    }

    fun onDeletePasswordChanged(v: String) {
        _state.value = _state.value.copy(deletePassword = v, error = null, info = null)
    }

    fun confirmDeleteAccount(onDeleted: () -> Unit) {
        val pwd = state.value.deletePassword
        if (pwd.length !in 8..63) {
            _state.value = _state.value.copy(error = "Пароль: 8–63 символа")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            userRepository.deleteAccount(pwd)
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false)
                    onDeleted()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка удаления аккаунта"
                    )
                }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            sessionRepository.logoutDevice()
                .onSuccess {
                    _state.value = _state.value.copy(isLoading = false)
                    onLoggedOut()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun logoutAllDevices() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            sessionRepository.logoutAll()
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        info = "Все устройства разлогинены"
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ошибка при разлогине устройств"
                    )
                }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, info = null)
    }
}

data class SettingsUiState(
    val currentDisplayName: String = "",
    val currentLogin: String = "",
    val newDisplayName: String = "",
    val deletePassword: String = "",
    val isLoading: Boolean = false,
    val isLoadingData: Boolean = false,
    val error: String? = null,
    val info: String? = null,

    val showDisplayNameDialog: Boolean = false,
    val showPasswordDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,

    val oldPassword: String = "",
    val newPassword: String = "",
    val newPasswordConfirm: String = ""
)
