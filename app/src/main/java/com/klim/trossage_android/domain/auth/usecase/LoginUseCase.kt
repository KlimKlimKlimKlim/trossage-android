package com.klim.trossage_android.domain.auth.usecase

import com.klim.trossage_android.core.util.Result
import com.klim.trossage_android.domain.auth.model.AuthToken
import com.klim.trossage_android.domain.auth.model.UserCredentials
import com.klim.trossage_android.domain.auth.repository.AuthRepository

class LoginUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credentials: UserCredentials): Result<AuthToken> {
        if (!credentials.isValid()) {
            val errors = credentials.getValidationErrors()
            val errorMessage = errors.joinToString("\n")
            return Result.Error(errorMessage)
        }
        return try {
            val token = authRepository.login(credentials)
            Result.Success(token)
        } catch (e: Exception) {
            Result.Error(
                message = mapErrorToMessage(e),
                exeption = e
            )
        }
    }
    private fun mapErrorToMessage(exception: Exception): String {
        val message = exception.message?.lowercase() ?: ""

        return when {
            message.contains("401") || message.contains("unauthorized") ->
                "Your ogin or password is wrong"
            message.contains("403") || message.contains("forbidden") ->
                "Access denied, contact our support"
            message.contains("500") || message.contains("503") ->
                "Server is temporarily unavailable. Try again later"
            message.contains("network") ||message.contains("unreachable") ->
                "Check your Internet connection"
            message.contains("timeout") ->
                "Timeout exceeded"
            else ->
                "Unable to log in. Try again later"
        }
    }
}