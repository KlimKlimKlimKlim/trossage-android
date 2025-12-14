package com.klim.trossage_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.websocket.ChatWebSocketManager
import com.klim.trossage_android.data.remote.websocket.TypingWebSocketManager
import com.klim.trossage_android.data.repository.AuthRepositoryImpl
import com.klim.trossage_android.data.repository.ChatRepositoryImpl
import com.klim.trossage_android.data.repository.MessageRepositoryImpl
import com.klim.trossage_android.domain.repository.AuthRepository
import com.klim.trossage_android.domain.repository.ChatRepository
import com.klim.trossage_android.domain.repository.MessageRepository
import com.klim.trossage_android.ui.auth.login.LoginViewModel
import com.klim.trossage_android.ui.auth.register.RegisterViewModel
import com.klim.trossage_android.ui.chat.list.ChatListViewModel
import com.klim.trossage_android.ui.navigation.NavGraph
import com.klim.trossage_android.ui.navigation.Screen
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var messageRepository: MessageRepository

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var chatListViewModel: ChatListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDependencies()

        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                val startDestination = if (authRepository.isLoggedIn()) {
                    Screen.ChatList.route
                } else {
                    Screen.Login.route
                }

                NavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    loginViewModel = loginViewModel,
                    registerViewModel = registerViewModel,
                    chatListViewModel = chatListViewModel,
                    messageRepository = messageRepository
                )
            }
        }
    }

    private fun setupDependencies() {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val baseHttpUrl = "https://trossage.teew.ru/api/"
        val baseWsUrl = "ws://192.168.1.100:8000"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseHttpUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ChatApiService::class.java)
        val authPrefs = AuthPreferences(applicationContext)

        val chatWebSocketManager = ChatWebSocketManager(authPrefs, baseWsUrl)
        val typingWebSocketManager = TypingWebSocketManager(authPrefs, baseWsUrl)

        authRepository = AuthRepositoryImpl(api, authPrefs)
        chatRepository = ChatRepositoryImpl(api, chatWebSocketManager, authPrefs)
        messageRepository = MessageRepositoryImpl(
            api,
            chatWebSocketManager,
            typingWebSocketManager,
            authPrefs
        )

        loginViewModel = LoginViewModel(authRepository)
        registerViewModel = RegisterViewModel(authRepository)
        chatListViewModel = ChatListViewModel(chatRepository)

    }
}
