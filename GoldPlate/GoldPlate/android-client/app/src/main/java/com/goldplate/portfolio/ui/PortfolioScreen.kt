package com.goldplate.portfolio.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goldplate.portfolio.network.Network
import com.goldplate.portfolio.data.TokenStorage
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PortfolioScreen(onLogout: ()->Unit, onSettings: ()->Unit) {
    val ctx = LocalContext.current
    val token = TokenStorage(ctx).getToken()
    val portfolio = remember { mutableStateOf<List<Map<String,Any>>>(listOf()) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (token == null) { onLogout(); return@LaunchedEffect }
        try {
            val resp = Network.api.getPortfolio("Bearer $token")
            if (resp.isSuccessful) {
                val body = resp.body()
                val list = body?.get("portfolio") as? List<Map<String,Any>> ?: listOf()
                portfolio.value = list
            } else {
                error.value = resp.errorBody()?.string() ?: "Failed"
            }
        } catch (e: Exception) {
            error.value = "Network error: ${e.localizedMessage}"
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Portfolio", style = MaterialTheme.typography.h6)
            Row {
                Button(onClick = onSettings) { Text("Settings") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { TokenStorage(ctx).clear(); onLogout() }) { Text("Logout") }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (error.value != null) Text(error.value!!, color = MaterialTheme.colors.error)
        //shares, price and quantity
        LazyColumn {
            items(portfolio.value) { item ->
                Card(Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${'$'}{item["symbol"]}")
                        Text("Shares: ${'$'}{item["shares"]}")
                        Text("Current: ${'$'}{item["current_price"]}")
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        AddStockArea(token)
    }
}

@Composable
fun AddStockArea(token: String?) {
    val symbol = remember { mutableStateOf("") }
    val shares = remember { mutableStateOf("") }
    val error = remember { mutableStateOf<String?>(null) }

    Column {
        OutlinedTextField(value = symbol.value, onValueChange = { symbol.value = it }, label = { Text("Symbol") })
        OutlinedTextField(value = shares.value, onValueChange = { shares.value = it }, label = { Text("Shares") })
        Button(onClick = {
            error.value = null
            if (symbol.value.isBlank() || shares.value.isBlank()) { error.value = "Fill fields"; return@Button }
            val s = shares.value.toDoubleOrNull()
            if (s == null) { error.value = "Shares must be a number"; return@Button }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = Network.api.addPortfolio("Bearer $token", mapOf("symbol" to symbol.value, "shares" to s))
                    if (resp.isSuccessful) {
                        // In a real app: refresh list. Here we keep it simple.
                    } else {
                        error.value = resp.errorBody()?.string() ?: "Failed to add"
                    }
                } catch (e: Exception) { error.value = "Network error" }
            }
        }) { Text("Add stock") }
        error.value?.let { Text(it, color = MaterialTheme.colors.error) }
    }
}
