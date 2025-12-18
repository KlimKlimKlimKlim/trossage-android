package com.klim.trossage_android.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onPasswordRepeatChanged(passwordRepeat: String) {
        _uiState.value = _uiState.value.copy(passwordRepeat = passwordRepeat, error = null)
    }

    fun onDisplayNameChanged(displayName: String) {
        _uiState.value = _uiState.value.copy(displayName = displayName, error = null)
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value

        val validationError = validateInput(
            state.username,
            state.password,
            state.passwordRepeat,
            state.displayName
        )
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            authRepository.register(state.username, state.password, state.displayName)
                .onSuccess {
                    _uiState.value = state.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка регистрации"
                    )
                }
        }
    }

    private fun validateInput(
        username: String,
        password: String,
        passwordRepeat: String,
        displayName: String
    ): String? {
        return when {
            username.isBlank() || password.isBlank() || displayName.isBlank() -> {
                "Заполните все поля"
            }
            username.length !in 3..20 -> {
                "Логин должен быть от 3 до 20 символов"
            }
            password.length !in 8..63 -> {
                "Пароль должен быть от 8 до 63 символов"
            }
            displayName.length !in 3..20 -> {
                "Отображаемое имя должно быть от 3 до 20 символов"
            }
            password != passwordRepeat -> {
                "Пароли не совпадают"
            }
            else -> null
        }
    }
}

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
