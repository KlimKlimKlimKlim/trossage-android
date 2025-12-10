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
                errors.add("Login can't be blank")
            }
            login.length < MIN_LOGIN_LENGTH -> {
                errors.add("Login must contain at least $MIN_LOGIN_LENGTH symbols")
            }
            login.length > MIN_LOGIN_LENGTH -> {
                errors.add("Login must not contain more than $MAX_LOGIN_LENGTH symbols")
            }
            !LOGIN_REGEX.matches(login) -> {
                errors.add("Login must consist of Latin letters, digits, _ and - only")
            }
        }
        if (password.isNotBlank()) {
            errors.add("Password can't be blank")
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Password must contain at least $MIN_PASSWORD_LENGTH symbols")
        }

        return errors
    }
}
