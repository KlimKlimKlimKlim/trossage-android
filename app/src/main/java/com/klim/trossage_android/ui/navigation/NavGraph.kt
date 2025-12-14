package com.klim.trossage_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.klim.trossage_android.domain.repository.MessageRepository
import com.klim.trossage_android.ui.auth.login.LoginScreen
import com.klim.trossage_android.ui.auth.login.LoginViewModel
import com.klim.trossage_android.ui.auth.register.RegisterScreen
import com.klim.trossage_android.ui.auth.register.RegisterViewModel
import com.klim.trossage_android.ui.chat.detail.ChatDetailScreen
import com.klim.trossage_android.ui.chat.detail.ChatDetailViewModel
import com.klim.trossage_android.ui.chat.list.ChatListScreen
import com.klim.trossage_android.ui.chat.list.ChatListViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatId}/{companionName}") {
        fun createRoute(chatId: String, companionName: String) =
            "chat_detail/$chatId/$companionName"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    chatListViewModel: ChatListViewModel,
    messageRepository: MessageRepository
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            ChatListScreen(
                viewModel = chatListViewModel,
                onChatClick = { chat ->
                    navController.navigate(
                        Screen.ChatDetail.createRoute(
                            chatId = chat.chatId,
                            companionName = chat.companionDisplayName
                        )
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
                navArgument("chatId") { type = NavType.StringType },
                navArgument("companionName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val companionName = backStackEntry.arguments?.getString("companionName") ?: ""

            val chatDetailViewModel = ChatDetailViewModel(chatId, messageRepository)

            ChatDetailScreen(
                viewModel = chatDetailViewModel,
                companionDisplayName = companionName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            androidx.compose.material3.Text("Settings - TODO")
        }
    }
}
