package com.starcode.locus.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.UsuarioEntity
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.LoginRequest
import com.starcode.locus.data.remote.request.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

// ✅ 1. AuthResult FUERA de la clase para que sea visible en NavGraph y Screens
sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState

    private val sessionManager = SessionManager(application)

    // --- FUNCIÓN DE LOGIN ---
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthResult.Error("Por favor, llena todos los campos")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (!response.token.isNullOrBlank()) {
                    sessionManager.guardarToken(response.token)
                    val user = response.usuario
                    dao.insertarUsuarios(listOf(
                        UsuarioEntity(
                            id_usuario = user.id_usuario,
                            nombre = user.nombre,
                            ape_pa = user.ape_pa,
                            ape_ma = user.ape_ma,
                            email = user.email,
                            password = ""
                        )
                    ))
                    _authState.value = AuthResult.Success(response.token)
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Error de red: Revisa tu conexión")
            }
        }
    }

    // --- ✅ 2. FUNCIÓN DE REGISTRO UNIFICADA ---
    // Esta es la que NavGraph está buscando y no encontraba
    fun registrarUsuario(
        nombre: String,
        paterno: String,
        materno: String,
        fecha: String,
        email: String,
        pass: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val request = RegisterRequest(
                    nombre = nombre,
                    ape_pa = paterno,
                    ape_ma = if (materno.isBlank()) null else materno,
                    fecha_nac = if (fecha.isBlank() || fecha == "Fecha de Nacimiento") null else fecha,
                    email = email,
                    password = pass
                )

                val response = RetrofitClient.instance.registrarUsuario(request)
                if (response.token.isNotEmpty()) {
                    sessionManager.guardarToken(response.token)
                    _authState.value = AuthResult.Success(response.token)
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error(e.localizedMessage ?: "Error en Railway")
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.borrarToken()
            dao.borrarTodosLosLugares()
            _authState.value = AuthResult.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthResult.Idle
    }
}