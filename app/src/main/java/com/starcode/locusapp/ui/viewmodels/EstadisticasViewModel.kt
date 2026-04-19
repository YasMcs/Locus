package com.starcode.locusapp.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locusapp.data.entities.VisitaHistorialDTO
import com.starcode.locusapp.data.remote.RetrofitClient
import com.starcode.locusapp.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EstadisticasViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _stats = MutableStateFlow(EstadisticasData())
    val stats: StateFlow<EstadisticasData> = _stats.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _lugaresVisitados = MutableStateFlow<List<VisitaHistorialDTO>>(emptyList())
    val lugaresVisitados: StateFlow<List<VisitaHistorialDTO>> = _lugaresVisitados.asStateFlow()

    data class EstadisticasData(
        val lugaresVisitados: Int = 0,
        val kmRecorridos: Double = 0.0,
        val totalPasos: Int = 0,
        val totalFotos: Int = 0,
        val categoriaRango: String = "Explorador"
    )

    init {
        cargarTodo()
    }

    fun cargarTodo() {
        val userId = sessionManager.getUserId()
        val tokenRaw = sessionManager.obtenerToken()

        if (userId <= 0 || tokenRaw.isNullOrBlank()) return

        val authHeader = "Bearer $tokenRaw"

        viewModelScope.launch {
            _estaCargando.value = true

            // --- LLAMADA 1: ESTADÍSTICAS GLOBALES ---
            try {
                val globales = RetrofitClient.instance.obtenerEstadisticasTotales(userId, authHeader)
                _stats.value = _stats.value.copy(
                    lugaresVisitados = globales.lugares_descubiertos,
                    kmRecorridos = globales.total_km,
                    totalPasos = globales.total_pasos,
                    categoriaRango = calcularRango(globales.lugares_descubiertos)
                )
            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error en Estadísticas: ${e.message}")
            }

            // --- LLAMADA 2: FOTOS ---
            try {
                val fotos = RetrofitClient.instance.obtenerImagenesUsuario(authHeader, userId)
                _stats.value = _stats.value.copy(totalFotos = fotos.size)
            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error en Fotos: ${e.message}")
            }

            // --- LLAMADA 3: HISTORIAL DE LUGARES ---
            try {
                // Si esta llamada falla por la ruta o el formato, ya no matará a las otras dos
                val historial = RetrofitClient.instance.obtenerLugaresVisitados(authHeader, userId)
                _lugaresVisitados.value = historial

            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error en Historial: ${e.message}")
            }

            _estaCargando.value = false
        }
    }

    private fun calcularRango(cantidad: Int): String {
        return when {
            cantidad > 20 -> "Leyenda Locus"
            cantidad > 10 -> "Explorador Élite"
            cantidad > 5 -> "Aventurero"
            else -> "Explorador"
        }
    }
}