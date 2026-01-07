package com.klim.trossage_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.klim.trossage_android.ui.auth.login.LoginScreen
import com.klim.trossage_android.ui.auth.login.LoginViewModel
import com.klim.trossage_android.ui.auth.register.RegisterScreen
import com.klim.trossage_android.ui.auth.register.RegisterViewModel
import com.klim.trossage_android.ui.chat.detail.ChatDetailScreen
import com.klim.trossage_android.ui.chat.detail.ChatDetailViewModel
import com.klim.trossage_android.ui.chat.list.ChatListScreen
import com.klim.trossage_android.ui.chat.list.ChatListViewModel
import com.klim.trossage_android.ui.settings.SettingsScreen
import com.klim.trossage_android.ui.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}/{companionName}") {
        fun createRoute(chatId: Int, companionName: String) = "chat_detail/$chatId/$companionName"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    settingsViewModel: SettingsViewModel,
    chatRepository: com.klim.trossage_android.domain.repository.ChatRepository,
    userRepository: com.klim.trossage_android.domain.repository.UserRepository,
    messageRepository: com.klim.trossage_android.domain.repository.MessageRepository
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ChatList.route) {
            val chatListViewModel = remember {
                ChatListViewModel(chatRepository, userRepository)
            }

            ChatListScreen(
                viewModel = chatListViewModel,
                onChatClick = { chat ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(chat.chatId, chat.companionDisplayName)
                    )
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.IntType },
                navArgument("companionName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0
            val companionName = backStackEntry.arguments?.getString("companionName") ?: ""

            val chatDetailViewModel = remember {
                ChatDetailViewModel(chatId, companionName, messageRepository)
            }

            ChatDetailScreen(
                viewModel = chatDetailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ChatList.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
