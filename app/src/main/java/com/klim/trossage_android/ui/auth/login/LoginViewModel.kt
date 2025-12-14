package com.klim.trossage_android.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klim.trossage_android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value

        val validationError = validateInput(state.username, state.password)
        if (validationError != null) {
            _uiState.value = state.copy(error = validationError)
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            authRepository.login(state.username, state.password)
                .onSuccess {
                    _uiState.value = state.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = error.message ?: "Ошибка входа"
                    )
                }
        }
    }

    private fun validateInput(username: String, password: String): String? {
        return when {
            username.isBlank() || password.isBlank() -> {
                "Заполните все поля"
            }
            username.length !in 3..20 -> {
                "Логин должен быть от 3 до 20 символов"
            }
            password.length !in 8..20 -> {
                "Пароль должен быть от 8 до 20 символов"
            }
            else -> null
        }
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
