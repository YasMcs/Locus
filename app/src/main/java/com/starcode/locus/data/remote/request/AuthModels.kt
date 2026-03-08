package com.starcode.locus.data.remote.request

data class AuthResponse(
    val token: String,
    val usuario: UsuarioResponse
)

data class UsuarioResponse(
    val id_usuario: Int,
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String?,
    val fecha_nac: String?,
    val email: String,
    val fecha_registro: String?
)

data class RegisterRequest(
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String? = null,
    val fecha_nac: String? = null,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}