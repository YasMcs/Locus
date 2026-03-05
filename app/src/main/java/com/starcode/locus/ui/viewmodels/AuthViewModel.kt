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

// Cambiamos a AndroidViewModel para tener acceso a 'application'
class AuthViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState

    // Inicializamos el SessionManager
    private val sessionManager = SessionManager(application)

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
                    // 1. PERSISTENCIA DINÁMICA: Guardamos el JWT en SharedPreferences
                    sessionManager.guardarToken(response.token)

                    // 2. PERSISTENCIA LOCAL: Guardamos los datos del usuario en Room
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
                } else {
                    _authState.value = AuthResult.Error("El servidor no devolvió un token válido")
                }

            } catch (e: HttpException) {
                val errorMsg = when(e.code()) {
                    401 -> "Correo o contraseña incorrectos"
                    404 -> "Servidor no encontrado"
                    else -> "Error: ${e.message()}"
                }
                _authState.value = AuthResult.Error(errorMsg)
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Error de red: Revisa tu conexión")
            }
        }
    }
    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.borrarToken()
            // Opcional: limpiar la base de datos de Room si quieres privacidad total
            dao.borrarTodosLosLugares()
            _authState.value = AuthResult.Idle // Reiniciar el estado
        }
    }
    fun registrar(nombre: String, apePa: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val request = RegisterRequest(
                    nombre = nombre,
                    ape_pa = apePa,
                    ape_ma = null,
                    fecha_nac = null,
                    email = email,
                    password = pass
                )
                val response = RetrofitClient.instance.registrarUsuario(request)

                if (!response.token.isNullOrBlank()) {
                    sessionManager.guardarToken(response.token)
                    _authState.value = AuthResult.Success(response.token)
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error("Error en el registro: ${e.message}")
            }
        }
    }
}

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}