package com.starcode.locus.data.remote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("locus_prefs", Context.MODE_PRIVATE)

    fun guardarToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString("auth_token", null)
    }
    fun borrarToken() {
        prefs.edit().remove("auth_token").apply()
    }
}