package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.ImagenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecuerdosViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _imagenes = MutableStateFlow<List<ImagenResponse>>(emptyList())
    val imagenes: StateFlow<List<ImagenResponse>> = _imagenes

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    // Función para traer las fotos del servidor
    // En tu RecuerdosViewModel.kt
    private val _debugStep = MutableStateFlow("Esperando...")
    val debugStep: StateFlow<String> = _debugStep

    fun cargarRecuerdos() {
        val userId = sessionManager.getUserId()
        val tokenRaw = sessionManager.obtenerToken()

        viewModelScope.launch {
            _estaCargando.value = true
            _debugStep.value = "Iniciando... ID: $userId"

            try {
                if (tokenRaw == null) {
                    _debugStep.value = "❌ Error: Token nulo"
                    return@launch
                }

                _debugStep.value = "📡 Llamando API (ID: $userId)..."

                val lista = RetrofitClient.instance.obtenerImagenesUsuario(
                    token = "Bearer $tokenRaw",
                    id = userId
                )

                _debugStep.value = "✅ Éxito: ${lista.size} fotos recibidas"
                _imagenes.value = lista

            } catch (e: Exception) {
                // Esto nos dirá si es 404, 500 o error de conexión
                _debugStep.value = "💥 ERROR: ${e.localizedMessage ?: "Desconocido"}"
                Log.e("LocusDebug", "Fallo: ", e)
            } finally {
                _estaCargando.value = false
            }
        }
    }
}