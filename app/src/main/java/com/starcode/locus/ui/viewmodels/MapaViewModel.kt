package com.starcode.locus.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.RecuerdoRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class MapaViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val sessionManager = SessionManager(application)

    init {
        cargarLugares()
    }

    fun cargarLugares() {
        viewModelScope.launch {
            try {
                val dbLugares = dao.obtenerLugares()
                _lugares.value = dbLugares
                if (dbLugares.isEmpty()) sincronizarConServidor()
            } catch (e: Exception) {
                Log.e("Locus", "Error al cargar: ${e.message}")
            }
        }
    }

    private fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = sessionManager.obtenerToken() ?: ""
                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares("Bearer $token")
                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)
                _lugares.value = dao.obtenerLugares()
            } catch (e: Exception) {
                Log.e("Locus", "Error sync: ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }

    // FUNCIÓN FINAL DE SUBIDA PARA POSTGRESQL
    fun subirRecuerdoCompleto(lugarId: Int, imagenPart: MultipartBody.Part) {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = sessionManager.obtenerToken() ?: ""
                val authHeader = "Bearer $token"

                // 1. Subir imagen
                val imagenResponse = RetrofitClient.instance.subirImagen(authHeader, imagenPart)

                if (imagenResponse != null) {
                    // 2. Crear vínculo del recuerdo
                    val recuerdoReq = RecuerdoRequest(
                        usuario_id = sessionManager.getUserId(),
                        lugar_id = lugarId,
                        imagen_id = imagenResponse.id
                    )
                    RetrofitClient.instance.crearRecuerdo(authHeader, recuerdoReq)
                }
            } catch (e: Exception) {
                Log.e("LocusAPI", "Error subida: ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }
}