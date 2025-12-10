package com.klim.trossage_android.domain.auth.model

import kotlin.math.min

data class UserCredentials (
    val login: String,
    val password: String
) {
    companion object {
        private val LOGIN_REGEX = "^[a-zA-Z][a-zA-Z0-9_-]*$".toRegex()
        private const val MIN_LOGIN_LENGTH = 3
        private const val MAX_LOGIN_LENGTH = 20
        private const val MIN_PASSWORD_LENGTH = 8
    }
    fun isLoginValid(): Boolean {
        return login.isNotBlank()
                && login.length in MIN_LOGIN_LENGTH..MAX_LOGIN_LENGTH
                && LOGIN_REGEX.matches(login)
    }
    fun isPasswordValid(): Boolean {
        return password.isNotBlank() && password.length >= MIN_PASSWORD_LENGTH
    }
    fun isValid(): Boolean {
        return isLoginValid() && isPasswordValid()
    }
    fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()

        when {
            login.isNotBlank() -> {
                errors.add("Логин не может быть пустым")
            }
            login.length < MIN_LOGIN_LENGTH -> {
                errors.add("Логин должен содержать минимум $MIN_LOGIN_LENGTH символа")
            }
            login.length > MIN_LOGIN_LENGTH -> {
                errors.add("Логин не должен содержать больше $MAX_LOGIN_LENGTH символов")
            }
            !LOGIN_REGEX.matches(login) -> {
                errors.add("Логин может содержать только латинские буквы, цифры, _ и -")
            }
        }
        if (password.isNotBlank()) {
            errors.add("Пароль не может быть пустым")
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Пароль должен содержать минимум $MIN_PASSWORD_LENGTH символов")
        }

        return errors
    }
}
