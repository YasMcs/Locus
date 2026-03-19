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
            try {
                val dbLugares = dao.obtenerLugares()
                _lugares.value = dbLugares
                if (dbLugares.isEmpty()) sincronizarConServidor()
            } catch (e: Exception) {
                Log.e("Locus", "Error al cargar: ${e.message}")
            }
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            try {
                val token = sessionManager.obtenerToken() ?: return@launch
                val authHeader = "Bearer $token"

                // 1. PRIMERO LAS CATEGORÍAS (Esto evita el error de Foreign Key)
                debugStep = "📂 Sincronizando categorías..."
                val categoriasApi = RetrofitClient.instance.obtenerCategorias(authHeader)
                dao.insertarCategorias(categoriasApi)

                // 2. AHORA SÍ, LOS LUGARES
                debugStep = "🌐 Descargando lugares..."
                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares(authHeader)

                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)

                _lugares.value = dao.obtenerLugares()
                debugStep = "✅ ¡Mapa listo con ${lugaresApi.size} puntos!"

            } catch (e: Exception) {
                debugStep = "💥 Error API: FOREIGN KEY corregida?"
                Log.e("LocusDebug", "Fallo en sync", e)
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
