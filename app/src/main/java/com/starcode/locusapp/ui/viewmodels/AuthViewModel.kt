package com.starcode.locusapp.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locusapp.data.dao.LocusDao
import com.starcode.locusapp.data.entities.UsuarioEntity
import com.starcode.locusapp.data.remote.RetrofitClient
import com.starcode.locusapp.data.remote.SessionManager
import com.starcode.locusapp.data.remote.request.LoginRequest
import com.starcode.locusapp.data.remote.request.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.widget.Toast
import retrofit2.HttpException

// Resultado de autenticación para la UI
sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

    class AuthViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {
        init {
            clearLegacyData()
        }
        private fun clearLegacyData() {
            try {
                val prefs = context.getSharedPreferences("locus_prefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
            } catch (e: Exception) {
                android.util.Log.e("LocusDebug", "Error limpiando datos legacy: ${e.message}")
            }
        }
        private val context = application.applicationContext

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
                val body = response.body()
                if (body != null && !body.token.isNullOrBlank()) {
                    sessionManager.guardarToken(body.token)
                    val user = body.usuario
                    sessionManager.guardarUserId(user.id_usuario)
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
                    _authState.value = AuthResult.Success(body.token)
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
        pass: String,
        genero: String = ""
    ) {
        println("DEBUG: 2. Entrando al ViewModel")
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                val request = RegisterRequest(
                    nombre = nombre,
                    ape_pa = paterno,
                    ape_ma = if (materno.isBlank()) null else materno,
                    fecha_nac = if (fecha.isBlank() || fecha == "Fecha de Nacimiento") null else fecha,
                    email = email,
                    password = pass,
                    genero = genero.takeIf { it.isNotBlank() }
                )

                println("DEBUG: 3. Lanzando petición Coroutine")
                android.util.Log.d("LocusDebug", "Intentando registro con: $request")
                println("DEBUG: 4. Llamando a ApiService con: $nombre, $email")
                val response = RetrofitClient.instance.registrarUsuario(request)
                
                println("DEBUG: Status code: ${response.code()}, body: ${response.body()}")
                if (response.isSuccessful) {
                    println("DEBUG: Registro exitoso en servidor: ${response.body()}")
                    val responseData = response.body()
                    if (responseData != null && responseData.token != null && responseData.token.isNotEmpty()) {
                        android.util.Log.d("LocusDebug", "Registro exitoso: $responseData")

                        sessionManager.guardarToken(responseData.token)
                        
                        responseData.usuario?.let { user ->
                            sessionManager.guardarUserId(user.id_usuario)
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
                        }
                        
                        Toast.makeText(context, "REGISTRO OK", Toast.LENGTH_SHORT).show()
                        _authState.value = AuthResult.Success(responseData.token)
                    } else {
                        _authState.value = AuthResult.Error("Token vacío del servidor")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG: Error del servidor (${response.code()}): $errorBody")
                    _authState.value = AuthResult.Error("Error servidor (${response.code()}): $errorBody")
                }
            } catch (e: HttpException) {
                Toast.makeText(context, "ERROR SERVER: ${e.code()}", Toast.LENGTH_SHORT).show()
                _authState.value = AuthResult.Error("Error servidor: ${e.code()}")
            } catch (e: Exception) {
                Toast.makeText(context, "ERROR CRÍTICO: ${e.message}", Toast.LENGTH_LONG).show()
                _authState.value = AuthResult.Error(e.localizedMessage ?: "Error al registrar")
            }
        }
    }

    // --- CERRAR SESIÓN ---
    fun cerrarSesion() {
        viewModelScope.launch {
            sessionManager.cerrarSesion()
            dao.borrarTodosLosUsuarios() // ✅ Limpia la DB local para que el Perfil se vacíe
            dao.borrarTodosLosLugares()
            _authState.value = AuthResult.Idle
        }
    }

    fun resetAuthState() {
        _authState.value = AuthResult.Idle
    }
}