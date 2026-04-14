package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EstadisticasViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _stats = MutableStateFlow(EstadisticasData())
    val stats: StateFlow<EstadisticasData> = _stats.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    data class EstadisticasData(
        val lugaresVisitados: Int = 0,
        val kmRecorridos: Double = 0.0,
        val totalPasos: Int = 0,
        val totalFotos: Int = 0,
        val categoriaRango: String = "Explorador"
    )

    /**
     * Llamamos a esta función cada vez que el usuario entra a la pantalla
     * o cuando queremos refrescar tras una actividad.
     */
    fun cargarEstadisticas() {
        val userId = sessionManager.getUserId()
        val tokenRaw = sessionManager.obtenerToken()

        if (userId <= 0 || tokenRaw.isNullOrBlank()) {
            Log.e("LocusDebug", "Estadisticas: Sesión no válida")
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = "Bearer $tokenRaw"

                // 1. Llamada al nuevo endpoint de estadísticas consolidadas del servidor
                val globales = RetrofitClient.instance.obtenerEstadisticasTotales(token, userId)

                // 2. Opcional: Seguimos trayendo las fotos si quieres mostrar el conteo de recuerdos
                val fotos = RetrofitClient.instance.obtenerImagenesUsuario(token, userId)

                // 3. Actualizamos el estado con DATOS REALES del backend
                _stats.value = EstadisticasData(
                    lugaresVisitados = globales.lugares_descubiertos,
                    kmRecorridos = globales.total_km,
                    totalPasos = globales.total_pasos,
                    totalFotos = fotos.size,
                    categoriaRango = calcularRango(globales.lugares_descubiertos)
                )

                Log.d("LocusDebug", "✅ Estadísticas reales cargadas desde el servidor")

            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error cargando estadísticas reales: ${e.message}")
            } finally {
                _estaCargando.value = false
            }
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