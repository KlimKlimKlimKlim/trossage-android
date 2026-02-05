package com.klim.trossage_android.data.remote.dto

data class TypingUpdateRequest(
    val operations: List<TypingOperationDto>
)

data class TypingOperationDto(
    val type: String,
    val position: Int? = null,
    val length: Int? = null,
    val text: String? = null
)
