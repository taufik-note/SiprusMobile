package com.example.utils

import android.content.Context

class SessionManager(context: Context) {
    private val pref = context.getSharedPreferences("siprus_pref", Context.MODE_PRIVATE)

    fun saveToken(token: String) = pref.edit().putString("token", token).apply()
    fun getToken(): String = pref.getString("token", "") ?: ""
    fun clearSession() = pref.edit().remove("token").apply()
    fun isLoggedIn(): Boolean = getToken().isNotEmpty()

    fun saveCredentials(email: String, password: String, remember: Boolean) {
        pref.edit().apply {
            putBoolean("remember_me", remember)
            if (remember) {
                putString("rem_email", email)
                putString("rem_password", password)
            } else {
                remove("rem_email")
                remove("rem_password")
            }
        }.apply()
    }

    fun getRememberedEmail(): String = pref.getString("rem_email", "") ?: ""
    fun getRememberedPassword(): String = pref.getString("rem_password", "") ?: ""
    fun isRememberMeChecked(): Boolean = pref.getBoolean("remember_me", false)
}