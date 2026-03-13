package com.starcode.locus.data.remote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("locus_prefs", Context.MODE_PRIVATE)

    // --- SEGURIDAD Y IDENTIDAD ---
    fun guardarToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun obtenerToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun guardarUserId(id: Int) {
        prefs.edit().putInt("user_id", id).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    // --- FLUJO DE USUARIO (LO QUE FALTABA) ---
    fun guardarEdadValidada(esMayor: Boolean) {
        prefs.edit().putBoolean("edad_validada", esMayor).apply()
    }

    fun esEdadValidada(): Boolean {
        return prefs.getBoolean("edad_validada", false)
    }

    // --- LIMPIEZA ---
    fun borrarToken() {
        prefs.edit().clear().apply()
    }
}