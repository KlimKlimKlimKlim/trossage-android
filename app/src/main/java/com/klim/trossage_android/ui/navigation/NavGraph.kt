package com.klim.trossage_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.klim.trossage_android.ui.auth.login.LoginScreen
import com.klim.trossage_android.ui.auth.login.LoginViewModel
import com.klim.trossage_android.ui.auth.register.RegisterScreen
import com.klim.trossage_android.ui.auth.register.RegisterViewModel
import com.klim.trossage_android.ui.chat.list.ChatListScreen
import com.klim.trossage_android.ui.chat.list.ChatListViewModel
import com.klim.trossage_android.ui.settings.SettingsScreen
import com.klim.trossage_android.ui.settings.SettingsViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ChatList : Screen("chat_list")
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
    userRepository: com.klim.trossage_android.domain.repository.UserRepository
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
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
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
