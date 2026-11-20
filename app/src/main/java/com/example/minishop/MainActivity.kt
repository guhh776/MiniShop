package com.example.minishop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.minishop.ui.screens.*
import com.example.minishop.ui.theme.MiniShopTheme

/**
 * Ilova ekranlari
 */
enum class AppScreen {
    LOGIN,
    REGISTER,
    SHOP,
    USERS_LIST,
    LOGIN_LOGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniShopTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var previousScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var loggedInUserLogin by remember { mutableStateOf("") }
    var cartCount by remember { mutableStateOf(0) }

    fun navigateTo(screen: AppScreen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    fun navigateBack() {
        currentScreen = previousScreen
    }

    // Android "Back" tugmasini boshqarish
    BackHandler(enabled = currentScreen != AppScreen.LOGIN && currentScreen != AppScreen.SHOP) {
        navigateBack()
    }

    when (currentScreen) {
        // 1. Alohida Login oynasi
        AppScreen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { login ->
                    loggedInUserLogin = login
                    currentScreen = AppScreen.SHOP
                },
                onNavigateToRegister = {
                    navigateTo(AppScreen.REGISTER)
                },
                onOpenUsers = {
                    navigateTo(AppScreen.USERS_LIST)
                },
                onOpenLogs = {
                    navigateTo(AppScreen.LOGIN_LOGS)
                }
            )
        }

        // 2. Alohida Ro'yxatdan o'tish oynasi
        AppScreen.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = {
                    currentScreen = AppScreen.LOGIN
                },
                onNavigateToLogin = {
                    currentScreen = AppScreen.LOGIN
                },
                onOpenUsers = {
                    navigateTo(AppScreen.USERS_LIST)
                },
                onOpenLogs = {
                    navigateTo(AppScreen.LOGIN_LOGS)
                }
            )
        }

        // 3. Foydalanuvchilar ro'yxati (signup jadvali) ekrani
        AppScreen.USERS_LIST -> {
            UsersListScreen(
                onBack = { navigateBack() }
            )
        }

        // 4. Kirish urinishlari tarixi (login jadvali) ekrani
        AppScreen.LOGIN_LOGS -> {
            LoginLogsScreen(
                onBack = { navigateBack() }
            )
        }

        // 5. Asosiy MiniShop ekrani
        AppScreen.SHOP -> {
            ShopHomeScreen(
                userLogin = loggedInUserLogin,
                cartCount = cartCount,
                onAddToCart = { cartCount++ },
                onLogout = {
                    loggedInUserLogin = ""
                    cartCount = 0
                    currentScreen = AppScreen.LOGIN
                },
                onOpenUsers = {
                    navigateTo(AppScreen.USERS_LIST)
                },
                onOpenLogs = {
                    navigateTo(AppScreen.LOGIN_LOGS)
                }
            )
        }
    }
}
