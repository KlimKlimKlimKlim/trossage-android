package com.klim.trossage_android.data.remote.api

import com.klim.trossage_android.data.remote.dto.*
import retrofit2.http.*

interface ChatApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthData>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthData>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<AuthData>

    @GET("chat")
    suspend fun getChats(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): ChatListResponse

    @POST("chat")
    suspend fun createChat(@Body request: CreateChatRequest): CreateChatResponse

    @DELETE("chat/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String)

    @GET("users/search")
    suspend fun searchUsers(@Query("query") query: String): List<UserDto>

    @GET("messages/{chatId}")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): MessageListResponse

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): SendMessageResponse
}
