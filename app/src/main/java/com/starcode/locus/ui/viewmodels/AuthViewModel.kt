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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Resultado de autenticación para la UI
sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState

    // ✅ ESTO ARREGLA EL PERFIL: Observa al usuario de la DB en tiempo real
    val usuarioLogueado: StateFlow<UsuarioEntity?> = dao.obtenerUsuarioFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val sessionManager = SessionManager(application)

    // --- LOGIN ---
    // --- LOGIN ---
    fun login(email: String, password: String) {
        // ... código anterior ...
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val response = RetrofitClient.instance.login(LoginRequest(email, password))
                if (!response.token.isNullOrBlank()) {
                    sessionManager.guardarToken(response.token)

                    val user = response.usuario

                    // ✅ ESTA ES LA LÍNEA QUE FALTABA:
                    sessionManager.guardarUserId(user.id_usuario)

                    // Guardar en la DB local (Room) para el perfil
                    dao.insertarUsuarios(listOf(
                        UsuarioEntity(
                            id_usuario = user.id_usuario,
                            nombre = user.nombre,
                            ape_pa = user.ape_pa,
                            ape_ma = user.ape_ma,
                            email = user.email,
                            password = "",
                            genero = user.genero,
                            fecha_nac = user.fecha_nac
                        )
                    ))
                    _authState.value = AuthResult.Success(response.token)
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Credenciales incorrectas o error de red")
            }
        }
    }

    // --- REGISTRO ---
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

                // Dentro de registrarUsuario
                val response = RetrofitClient.instance.registrarUsuario(request)
                if (response.token.isNotEmpty()) {
                    sessionManager.guardarToken(response.token)
                    // ✅ OPCIONAL: Si el registro te devuelve el id_usuario, guárdalo aquí también
                    // sessionManager.guardarUserId(response.usuario.id_usuario)
                    _authState.value = AuthResult.Success(response.token)
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error(e.localizedMessage ?: "Error al registrar")
            }
        }
    }

    // --- CERRAR SESIÓN ---
    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.borrarToken()
            dao.borrarTodosLosUsuarios() // ✅ Limpia la DB local para que el Perfil se vacíe
            dao.borrarTodosLosLugares()
            _authState.value = AuthResult.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthResult.Idle
    }
}