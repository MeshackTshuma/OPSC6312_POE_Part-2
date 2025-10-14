package com.goldplate.portfolio.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.goldplate.portfolio.network.Network
import com.goldplate.portfolio.data.TokenStorage
import androidx.compose.ui.platform.LocalContext
//register form
@Composable
fun RegisterScreen(onRegistered: ()->Unit, onBack: ()->Unit) {
    val ctx = LocalContext.current
    val email = remember { mutableStateOf("") }
    val displayName = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirm = remember { mutableStateOf("") }
    val error = remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Register", style = MaterialTheme.typography.h5)
        OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") })
        OutlinedTextField(value = displayName.value, onValueChange = { displayName.value = it }, label = { Text("Display name") })
        OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("Password") })
        OutlinedTextField(value = confirm.value, onValueChange = { confirm.value = it }, label = { Text("Confirm") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            error.value = null
            if (email.value.isBlank() || password.value.isBlank() || confirm.value.isBlank()) { error.value = "Fill fields"; return@Button }
            if (password.value != confirm.value) { error.value = "Passwords do not match"; return@Button }
            if (password.value.length < 6) { error.value = "Password too short"; return@Button }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = Network.api.register(mapOf("email" to email.value, "password" to password.value, "displayName" to displayName.value))
                    if (resp.isSuccessful) {
                        val token = resp.body()?.token
                        if (token != null) {
                            TokenStorage(ctx).saveToken(token)
                            onRegistered()
                        } else {
                            error.value = "Invalid server response"
                        }
                    } else {
                        error.value = resp.errorBody()?.string() ?: "Register failed"
                    }
                } catch (e: Exception) {
                    error.value = "Network error: ${e.localizedMessage}"
                }
            }
        }) { Text("Register") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) { Text("Back to login") }
        error.value?.let { Text(text = it, color = MaterialTheme.colors.error) }
    }
}
