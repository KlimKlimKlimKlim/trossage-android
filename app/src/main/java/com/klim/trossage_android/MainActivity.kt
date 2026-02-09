package com.klim.trossage_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.klim.trossage_android.ui.theme.TrossageTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.klim.trossage_android.data.local.preferences.AuthPreferences
import com.klim.trossage_android.data.remote.api.ChatApiService
import com.klim.trossage_android.data.remote.network.AuthHeaderInterceptor
import com.klim.trossage_android.data.remote.network.TokenRefreshAuthenticator
import com.klim.trossage_android.data.repository.AuthRepositoryImpl
import com.klim.trossage_android.data.repository.ChatRepositoryImpl
import com.klim.trossage_android.data.repository.SessionRepositoryImpl
import com.klim.trossage_android.data.repository.UserRepositoryImpl
import com.klim.trossage_android.data.repository.MessageRepositoryImpl
import com.klim.trossage_android.domain.repository.AuthRepository
import com.klim.trossage_android.domain.repository.ChatRepository
import com.klim.trossage_android.domain.repository.SessionRepository
import com.klim.trossage_android.domain.repository.UserRepository
import com.klim.trossage_android.ui.auth.login.LoginViewModel
import com.klim.trossage_android.ui.auth.register.RegisterViewModel
import com.klim.trossage_android.ui.navigation.NavGraph
import com.klim.trossage_android.ui.navigation.Screen
import com.klim.trossage_android.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import androidx.room.Room
import com.klim.trossage_android.data.local.room.AppDatabase
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


class MainActivity : ComponentActivity() {

    private lateinit var authPrefs: AuthPreferences
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var chatRepository: ChatRepository

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var messageRepository: com.klim.trossage_android.domain.repository.MessageRepository


    private val sessionExpiredFlow = MutableSharedFlow<Unit>()
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDependencies()

        setContent {
            val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()

            TrossageTheme(darkTheme = darkModeEnabled) {
                val nav = rememberNavController()
                navController = nav

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        sessionExpiredFlow.collect {
                            nav.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                val startDestination = if (authRepository.isLoggedIn()) {
                    Screen.ChatList.route
                } else {
                    Screen.Login.route
                }

                NavGraph(
                    navController = nav,
                    startDestination = startDestination,
                    loginViewModel = loginViewModel,
                    registerViewModel = registerViewModel,
                    settingsViewModel = settingsViewModel,
                    chatRepository = chatRepository,
                    userRepository = userRepository,
                    messageRepository = messageRepository,
                    authPrefs = authPrefs
                )
            }
        }

    }

    private fun setupDependencies() {
        val baseApi = "https://trossage.teew.ru/api/"

        authPrefs = AuthPreferences(applicationContext)

        val gson = GsonBuilder()
            .create()

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API_LOG", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authHeaderInterceptor = AuthHeaderInterceptor(authPrefs, baseApi, gson)

        val tokenAuthenticator = TokenRefreshAuthenticator(
            authPrefs = authPrefs,
            baseUrl = baseApi,
            onSessionExpired = {
                lifecycleScope.launch {
                    sessionExpiredFlow.emit(Unit)
                }
            },
            gson = gson
        )

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseApi)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val api = retrofit.create(ChatApiService::class.java)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "trossage_db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val chatDao = database.chatDao()
        val messageDao = database.messageDao()

        authRepository = AuthRepositoryImpl(api, authPrefs)
        userRepository = UserRepositoryImpl(api, authPrefs)
        sessionRepository = SessionRepositoryImpl(api, authPrefs)
        chatRepository = ChatRepositoryImpl(api, chatDao)
        messageRepository = MessageRepositoryImpl(api, messageDao, authPrefs)

        loginViewModel = LoginViewModel(authRepository)
        registerViewModel = RegisterViewModel(authRepository)
        settingsViewModel = SettingsViewModel(userRepository, sessionRepository, authRepository, authPrefs)
    }

}
