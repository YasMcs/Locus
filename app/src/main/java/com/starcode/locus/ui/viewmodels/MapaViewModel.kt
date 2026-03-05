package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapaViewModel(private val dao: LocusDao) : ViewModel() {

    // Estado para la lista de lugares
    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    // Estado para saber si estamos cargando datos
    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    init {
        // Al iniciar, cargamos lo que haya en la base de datos local
        cargarLugaresDesdeDB()
        // Y sincronizamos con el servidor
        sincronizarConServidor()
    }

    private fun cargarLugaresDesdeDB() {
        viewModelScope.launch {
            _lugares.value = dao.obtenerLugares()
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                Log.d("LocusDebug", "Iniciando petición al servidor...")

                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares()

                if (lugaresApi.isEmpty()) {
                    Log.d("LocusDebug", "⚠️ La lista llegó VACÍA. Revisa si Kian tiene datos en la tabla.")
                } else {
                    lugaresApi.forEach {
                        Log.d("LocusDebug", "✅ Recibido del servidor: ${it.nombre_lugar}")
                    }

                    dao.borrarTodosLosLugares()
                    dao.insertarLugares(lugaresApi)
                    _lugares.value = dao.obtenerLugares()
                    Log.d("LocusDebug", "DB Local actualizada con ${lugaresApi.size} lugares.")
                }

            } catch (e: Exception) {
                // ESTO ES LO QUE TIENES QUE ACTUALIZAR:
                Log.e("LocusDebug", "❌ ERROR FATAL EN SINCRONIZACIÓN")
                Log.e("LocusDebug", "Mensaje: ${e.message}")
                Log.e("LocusDebug", "Causa: ${e.cause}")
                e.printStackTrace() // Esto imprime toda la ruta del error en rojo en el Logcat
            } finally {
                _estaCargando.value = false
            }
        }
    }
}