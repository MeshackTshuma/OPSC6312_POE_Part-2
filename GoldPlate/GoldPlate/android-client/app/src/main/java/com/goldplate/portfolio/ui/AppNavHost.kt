package com.goldplate.portfolio.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
//functions tha define UI Components
@Composable
fun AppNavHost() {
    val screen = remember { mutableStateOf("login") }
    when (screen.value) {
        "login" -> LoginScreen(onLoggedIn = { screen.value = "main" }, onRegister = { screen.value = "register" })
        "register" -> RegisterScreen(onRegistered = { screen.value = "main" }, onBack = { screen.value = "login" })
        "main" -> PortfolioScreen(onLogout = { screen.value = "login" }, onSettings = { screen.value = "settings" })
        "settings" -> SettingsScreen(onBack = { screen.value = "main" })
    }
}
