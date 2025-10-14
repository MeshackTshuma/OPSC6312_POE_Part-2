package com.goldplate.portfolio.data

import android.content.Context

class TokenStorage(context: Context) {
    private val prefs = context.getSharedPreferences("goldplate_prefs", Context.MODE_PRIVATE)
    fun saveToken(token: String) = prefs.edit().putString("jwt", token).apply()
    fun getToken(): String? = prefs.getString("jwt", null)
    fun clear() = prefs.edit().remove("jwt").apply()
}
