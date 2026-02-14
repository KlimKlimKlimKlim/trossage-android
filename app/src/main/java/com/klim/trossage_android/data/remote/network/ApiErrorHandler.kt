package com.klim.trossage_android.data.remote.network

import com.google.gson.Gson
import com.klim.trossage_android.data.remote.dto.ApiResponse
import retrofit2.HttpException
import java.io.IOException

object ApiErrorHandler {
    private val gson = Gson()

    fun handleError(e: Exception): String {
        return when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    if (errorBody != null) {
                        val apiError = gson.fromJson(errorBody, ApiResponse::class.java)
                        apiError.error ?: "HTTP ${e.code()}"
                    } else {
                        "HTTP ${e.code()}"
                    }
                } catch (parseError: Exception) {
                    "HTTP ${e.code()}"
                }
            }
            is IOException -> "Ошибка сети"
            else -> e.message ?: "Неизвестная ошибка"
        }
    }
}
