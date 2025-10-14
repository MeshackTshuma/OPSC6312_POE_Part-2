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
//login page
@Composable
fun LoginScreen(onLoggedIn: ()->Unit, onRegister: ()->Unit) {
    val ctx = LocalContext.current
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val loading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Login", style = MaterialTheme.typography.h5)
        OutlinedTextField(value = email.value, onValueChange = { email.value = it }, label = { Text("Email") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password.value, onValueChange = { password.value = it }, label = { Text("Password") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            error.value = null
            if (email.value.isBlank() || password.value.isBlank()) { error.value = "Fill all fields"; return@Button }
            loading.value = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = Network.api.login(mapOf("email" to email.value, "password" to password.value))
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        val token = body?.token
                        if (token != null) {
                            TokenStorage(ctx).saveToken(token)
                            loading.value = false
                            onLoggedIn()
                        } else {
                            loading.value = false
                            error.value = "Invalid server response"
                        }
                    } else {
                        loading.value = false
                        val msg = resp.errorBody()?.string() ?: "Login failed"
                        error.value = msg
                    }
                } catch (e: Exception) {
                    loading.value = false
                    error.value = "Network error: ${e.localizedMessage}"
                }
            }
        }) { Text(if (loading.value) "Please wait..." else "Login") }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRegister) { Text("Register") }
        error.value?.let { Text(text = it, color = MaterialTheme.colors.error) }
    }
}
