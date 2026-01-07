package com.klim.trossage_android.data.remote.api

import com.klim.trossage_android.data.remote.dto.*
import com.klim.trossage_android.data.remote.network.AuthType
import retrofit2.http.*

interface ChatApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginUserRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterUserRequest): RegisterResponse

    @Headers("${AuthType.HEADER}: ${AuthType.REFRESH}")
    @POST("auth/refresh")
    suspend fun refresh(): ApiResponse<TokenResponse>

    @Headers("${AuthType.HEADER}: ${AuthType.REFRESH}")
    @POST("auth/logout")
    suspend fun logoutDevice(): ApiResponse<Any>

    @POST("auth/logout-all")
    suspend fun logoutAll(): ApiResponse<Any>

    @GET("users/me")
    suspend fun getMe(): ApiResponse<UserResponse>

    @PATCH("users/me")
    suspend fun updateDisplayName(@Body request: UpdateDisplayNameRequest): ApiResponse<UpdateUserResponse>

    @PATCH("users/me")
    suspend fun updatePassword(@Body request: UpdatePasswordRequest): ApiResponse<UpdateUserResponse>

    @HTTP(method = "DELETE", path = "users/me", hasBody = true)
    suspend fun deleteMe(@Body request: DeleteUserRequest): ApiResponse<Any>

    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponse<UsersSearchResponse>

    @GET("chats")
    suspend fun getChats(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponse<ChatsListResponse>

    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequest): ApiResponse<ChatResponse>

    @GET("chats/{chat_id}/messages")
    suspend fun getMessages(
        @Path("chat_id") chatId: Int,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponse<MessagesResponse>

    @POST("chats/{chat_id}/messages")
    suspend fun sendMessage(
        @Path("chat_id") chatId: Int,
        @Body request: SendMessageRequest
    ): ApiResponse<MessageResponse>

}
