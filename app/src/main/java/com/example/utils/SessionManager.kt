package com.example.utils

import android.content.Context

class SessionManager(context: Context) {
    private val pref = context.getSharedPreferences("siprus_pref", Context.MODE_PRIVATE)

    fun saveToken(token: String) = pref.edit().putString("token", token).apply()
    fun getToken(): String = pref.getString("token", "") ?: ""
    fun clearSession() = pref.edit().clear().apply()
    fun isLoggedIn(): Boolean = getToken().isNotEmpty()
}