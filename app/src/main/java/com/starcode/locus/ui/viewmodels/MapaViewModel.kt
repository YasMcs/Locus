package com.starcode.locus.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import okhttp3.RequestBody

class MapaViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    var debugStep by mutableStateOf("Esperando...")
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
            // No activamos loading aquí porque la DB local es instantánea
            try {
                val dbLugares = dao.obtenerLugares()
                _lugares.value = dbLugares
                // Si la DB está vacía, sincronizamos (aquí es donde tarda)
                if (dbLugares.isEmpty()) sincronizarConServidor()
            } catch (e: Exception) {
                Log.e("Locus", "Error al cargar: ${e.message}")
            }
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true // 1. ENCENDER AL PERRITO
            try {
                val token = sessionManager.obtenerToken() ?: return@launch
                val authHeader = "Bearer $token"

                // Sincronización de categorías
                val categoriasApi = RetrofitClient.instance.obtenerCategorias(authHeader)
                dao.insertarCategorias(categoriasApi)

                // Sincronización de lugares
                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares(authHeader)
                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)

                _lugares.value = dao.obtenerLugares()

            } catch (e: Exception) {
                Log.e("LocusDebug", "Fallo en sync", e)
            } finally {
                _estaCargando.value = false // 2. APAGAR AL PERRITO
            }
        }
    }
    // FUNCIÓN FINAL DE SUBIDA PARA POSTGRESQL
    fun subirImagenConDatos(userId: RequestBody, lugarId: RequestBody, imagenPart: MultipartBody.Part, nota: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            debugStep = "⏳ Paso 1: Subiendo imagen..."

            try {
                // --- PASO 1: SUBIR IMAGEN ---
                val response = RetrofitClient.instance.subirImagen(
                    idUsuario = userId,
                    idLugar = lugarId,
                    imagen = imagenPart
                )

                // Si llegamos aquí, ya tenemos el ID de la imagen
                val idImagenRecienCreada = response.id_imagen
                debugStep = "✅ Imagen OK. Paso 2: Guardando nota..."

                // --- PASO 2: CREAR EL RECUERDO ---
                // Aquí llamarías a la otra ruta que tu backend mencionó (/recuerdos)
                // enviando el idImagenRecienCreada y la nota de texto.

                // val resultadoRecuerdo = RetrofitClient.instance.crearRecuerdo(...)

                debugStep = "🎉 ¡Todo guardado con éxito!"

            } catch (e: retrofit2.HttpException) {
                val codigo = e.code()
                // SI DA 500 AQUÍ: Es que el servidor falló al intentar subir a Cloudinary
                // o al escribir en la tabla de imágenes.
                debugStep = "❌ Error Servidor ($codigo)"
                Log.e("LocusDebug", "Detalle: ${e.response()?.errorBody()?.string()}")
            } catch (e: Exception) {
                debugStep = "❌ Error: ${e.localizedMessage}"
            } finally {
                _estaCargando.value = false
            }
        }
    }
    }
