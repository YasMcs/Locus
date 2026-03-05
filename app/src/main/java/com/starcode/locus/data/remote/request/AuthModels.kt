package com.starcode.locus.data.remote.request

import com.starcode.locus.data.entities.UsuarioEntity

/**
 * Este objeto es lo que le enviaremos al servidor
 * cuando el usuario intente iniciar sesión.
 */
data class LoginRequest(
    val email: String,
    val pass: String
)

/**
 * Este objeto es lo que el servidor nos responderá.
 * success: si todo salió bien (true/false)
 * message: "Bienvenido" o "Contraseña incorrecta"
 * token: una clave de seguridad que envían las APIs (opcional por ahora)
 */
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)