package com.goldplate.portfolio.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goldplate.portfolio.data.TokenStorage
import com.goldplate.portfolio.network.Network
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
//settings screen to allow users to change thier settings
@Composable
fun SettingsScreen(onBack: ()->Unit) {
    val ctx = LocalContext.current
    val token = TokenStorage(ctx).getToken()
    val displayName = remember { mutableStateOf("") }
    val theme = remember { mutableStateOf("light") }
    val message = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val resp = Network.api.getSettings("Bearer $token")
            if (resp.isSuccessful) {
                val body = resp.body()
                val settings = body?.get("settings") as? Map<String,Any>
                displayName.value = settings?.get("displayName") as? String ?: ""
                theme.value = settings?.get("theme") as? String ?: "light"
            }
        } catch (_: Exception) { }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.h6)
        OutlinedTextField(value = displayName.value, onValueChange = { displayName.value = it }, label = { Text("Display name") })
        Spacer(Modifier.height(8.dp))
        Row { Text("Theme: ") ; Text(theme.value) }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = Network.api.setSettings("Bearer $token", mapOf("settings" to mapOf("displayName" to displayName.value, "theme" to theme.value)))
                    if (resp.isSuccessful) message.value = "Saved"
                } catch (e: Exception) { message.value = "Failed" }
            }
        }) { Text("Save") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack) { Text("Back") }
        message.value?.let { Text(it) }
    }
}
