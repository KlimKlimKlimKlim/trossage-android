package com.klim.trossage_android.data.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    )

    fun parseIsoToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return System.currentTimeMillis()

        for (format in formats) {
            try {
                return format.parse(iso)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                continue
            }
        }

        return System.currentTimeMillis()
    }

    fun formatTime(millis: Long): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(Date(millis))
    }

    fun formatDate(millis: Long): String {
        val today = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { timeInMillis = millis }

        return when {
            today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) -> {
                formatTime(millis)
            }
            today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) - 1 == messageDate.get(Calendar.DAY_OF_YEAR) -> {
                "Вчера"
            }
            else -> {
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))
            }
        }
    }
}
