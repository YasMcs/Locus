package com.starcode.locus.data.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val usuario: UsuarioResponse // <-- Esto quita el error de 'response.usuario'
)

@Serializable
data class UsuarioResponse(
    val id_usuario: Int,
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String?,
    val fecha_nac: String?,
    val email: String,
    val fecha_registro: String?
)

@Serializable
data class RegisterRequest( // <-- Asegúrate que se llame así para quitar el rojo del import
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String? = null,
    val fecha_nac: String? = null,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)