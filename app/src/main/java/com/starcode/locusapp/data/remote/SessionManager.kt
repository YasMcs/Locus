package com.starcode.locusapp.data.remote

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("locus_prefs", Context.MODE_PRIVATE)

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

    fun guardarEdadValidada(esMayor: Boolean) {
        prefs.edit().putBoolean("edad_validada", esMayor).apply()
    }

    fun esEdadValidada(): Boolean {
        return prefs.getBoolean("edad_validada", false)
    }

    // ✅ NUEVA FUNCIÓN: Limpieza total para Logout
    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    // Guarda que ya aceptó las políticas
    fun guardarPrivacidadAceptada(aceptada: Boolean) {
        // Usamos "prefs" que es el nombre que definiste arriba
        prefs.edit().putBoolean("politicas_aceptadas", aceptada).apply()
    }

    // Consulta si ya las aceptó anteriormente
    fun esPrivacidadAceptada(): Boolean {
        // Usamos "prefs" en lugar de sharedPreferences
        return prefs.getBoolean("politicas_aceptadas", false)
    }
}