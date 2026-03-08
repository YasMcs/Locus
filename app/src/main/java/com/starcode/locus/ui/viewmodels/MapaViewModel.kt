package com.starcode.locus.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapaViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    // --- ESTADOS DE UI ---
    var debugStep by mutableStateOf("Iniciando...")

    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val sessionManager = SessionManager(application)

    init {
        Log.d("LocusDebug", "🚀 MapaViewModel instanciado correctamente")
        // No hace falta llamar a nada aquí si ya lo llamas desde el LaunchedEffect de la Screen
    }

    // --- FUNCIÓN QUE LLAMA LA SCREEN (Corregida) ---
    fun cargarLugares() {
        debugStep = "1. Llamando a obtenerLugares..."
        viewModelScope.launch {
            try {
                // ✅ Usamos el nombre exacto de tu LocusDao
                val respuesta = dao.obtenerLugares()

                if (respuesta.isEmpty()) {
                    debugStep = "⚠️ DB Vacía - Sincronizando..."
                    sincronizarConServidor()
                } else {
                    debugStep = "✅ Éxito: ${respuesta.size} lugares cargados"
                    _lugares.value = respuesta
                    // Intentamos actualizar con el servidor en segundo plano
                    sincronizarConServidor()
                }
            } catch (e: Exception) {
                debugStep = "❌ Error: ${e.localizedMessage}"
                Log.e("LocusDebug", "Error en cargarLugares", e)
            }
        }
    }

    // En MapaViewModel.kt
    fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = sessionManager.obtenerToken() ?: return@launch
                val authHeader = "Bearer $token"

                // 1. Primero las categorías (¡Obligatorio por la ForeignKey!)
                debugStep = "📂 Sincronizando categorías..."
                val cats = RetrofitClient.instance.obtenerCategorias(authHeader)
                dao.insertarCategorias(cats)

                // 2. Ahora los lugares
                debugStep = "🌐 Descargando lugares..."
                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares(authHeader)

                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)

                _lugares.value = dao.obtenerLugares()
                debugStep = "✅ ¡Mapa listo con ${lugaresApi.size} puntos!"

            } catch (e: Exception) {
                debugStep = "❌ Error: ${e.javaClass.simpleName}"
                Log.e("LocusDebug", "Fallo en sync", e)
            } finally {
                _estaCargando.value = false
            }
        }
    }
}