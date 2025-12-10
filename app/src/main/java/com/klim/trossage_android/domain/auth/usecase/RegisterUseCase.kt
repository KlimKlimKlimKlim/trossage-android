package com.klim.trossage_android.domain.auth.usecase

import com.klim.trossage_android.core.util.Result
import com.klim.trossage_android.domain.auth.model.AuthToken
import com.klim.trossage_android.domain.auth.model.UserCredentials
import com.klim.trossage_android.domain.auth.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credentials: UserCredentials): Result<AuthToken> {
        if (!credentials.isValid()) {
            val errors = credentials.getValidationErrors()
            val errorMessage = errors.joinToString("\n")
            return Result.Error(errorMessage)
        }
        val registrationError = validateForRegistration(credentials)
        if (registrationError != null) {
            return Result.Error(registrationError)
        }
        return try {
            val token = authRepository.register(credentials)
            Result.Success(token)
        } catch(e: Exception) {
            Result.Error(
                message = mapErrorToMessage(e),
                exception = e
            )
        }
    }
    private fun validateForRegistration(credentials: UserCredentials): String? {
        val reservedLogins = setOf(
            "admin",
            "root",
            "system",
            "support",
            "moderator",
            "owner",
            "administrator"
        )
        if (credentials.login.lowercase() in reservedLogins) {
            return "This login is reserved by the system"
        }
        if (credentials.password.lowercase().contains(credentials.login.lowercase())) {
            return "Your password must not contain your login"
        }
        return null
    }
    private fun mapErrorToMessage(exception: Exception): String {
        val message = exception.message?.lowercase() ?: ""

        return when {
            message.contains("409") || message.contains("conflict") ->
                "This login is already taken. Try another"
            message.contains("422") || message.contains("unprocessable") ->
                "Your login or password does not meet the demands"
            message.contains("400") || message.contains("bad request") ->
                "Incorrect information. Check if you entered everything right"
            message.contains("500") || message.contains("503") ->
                "Server is temporarily unavailable. Try again later"
            message.contains("network") ||message.contains("unreachable") ->
                "Check your Internet connection"
            message.contains("timeout") ->
                "Timeout exceeded"
            else ->
                "Unable to register. Try again later"
        }
    }
}