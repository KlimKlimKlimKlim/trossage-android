package com.klim.trossage_android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateChatRequest(
    @SerializedName("user_id") val userId: Int
)
