package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.ImagenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecuerdosViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _imagenes = MutableStateFlow<List<ImagenResponse>>(emptyList())
    val imagenes: StateFlow<List<ImagenResponse>> = _imagenes.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    // --- NOTA: Hemos eliminado _estadisticas y actualizarEstadisticasLocales ---
    // porque ahora usamos EstadisticasViewModel para leer datos reales del servidor.

    fun cargarRecuerdos() {
        val userId = sessionManager.getUserId()
        val tokenRaw = sessionManager.obtenerToken()

        // Verificamos que el usuario tenga sesión activa
        if (userId <= 0 || tokenRaw.isNullOrBlank()) {
            Log.e("LocusDebug", "Recuerdos: Sesión no válida")
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = "Bearer $tokenRaw"

                // Traemos la lista de imágenes/recuerdos desde la API
                val lista = RetrofitClient.instance.obtenerImagenesUsuario(token, userId)

                _imagenes.value = lista
                Log.d("LocusDebug", "✅ Recuerdos cargados: ${lista.size}")

            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error al cargar recuerdos: ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }
}