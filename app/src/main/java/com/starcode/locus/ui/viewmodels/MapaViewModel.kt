package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapaViewModel(private val dao: LocusDao) : ViewModel() {

    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    init {
        Log.d("LocusDebug", "✅ MapaViewModel creado")
        cargarLugaresDesdeDB()
        sincronizarConServidor()
    }

    private fun cargarLugaresDesdeDB() {
        viewModelScope.launch {
            val lugaresLocales = dao.obtenerLugares()
            Log.d("LocusDebug", "📦 Lugares en DB local: ${lugaresLocales.size}")
            _lugares.value = lugaresLocales
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                Log.d("LocusDebug", "🌐 Iniciando petición al servidor...")
                Log.d("LocusDebug", "🔑 Verificando token antes de petición...")

                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares()

                Log.d("LocusDebug", "📡 Respuesta recibida. Cantidad: ${lugaresApi.size}")

                if (lugaresApi.isEmpty()) {
                    Log.w("LocusDebug", "⚠️ La lista llegó VACÍA.")
                    Log.w("LocusDebug", "   Posibles causas:")
                    Log.w("LocusDebug", "   1. No hay lugares en la BD del servidor")
                    Log.w("LocusDebug", "   2. El token JWT no se está enviando")
                    Log.w("LocusDebug", "   3. El servidor respondió 401 sin lanzar excepción")
                } else {
                    lugaresApi.forEach {
                        Log.d("LocusDebug", "✅ Lugar recibido: ${it.nombre_lugar} (lat: ${it.latitud}, lon: ${it.longitud})")
                    }
                    dao.borrarTodosLosLugares()
                    dao.insertarLugares(lugaresApi)
                    _lugares.value = dao.obtenerLugares()
                    Log.d("LocusDebug", "✅ DB Local actualizada con ${lugaresApi.size} lugares")
                }

            } catch (e: retrofit2.HttpException) {
                // ✅ AGREGADO: captura específica de errores HTTP como 401, 403, 500
                Log.e("LocusDebug", "❌ ERROR HTTP: Código ${e.code()}")
                Log.e("LocusDebug", "   Mensaje: ${e.message()}")
                when (e.code()) {
                    401 -> Log.e("LocusDebug", "   🔑 ERROR 401: Token inválido o no enviado")
                    403 -> Log.e("LocusDebug", "   🚫 ERROR 403: Sin permisos")
                    500 -> Log.e("LocusDebug", "   💥 ERROR 500: Error interno del servidor")
                }
            } catch (e: java.net.ConnectException) {
                Log.e("LocusDebug", "❌ ERROR DE CONEXIÓN: No se pudo conectar al servidor")
                Log.e("LocusDebug", "   Verifica que la API esté corriendo y la IP sea correcta")
                Log.e("LocusDebug", "   URL actual: http://192.168.1.167:8080/api/lugares")
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("LocusDebug", "❌ TIMEOUT: El servidor tardó demasiado en responder")
            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ ERROR DESCONOCIDO: ${e.javaClass.simpleName}")
                Log.e("LocusDebug", "   Mensaje: ${e.message}")
                Log.e("LocusDebug", "   Causa: ${e.cause}")
                e.printStackTrace()
            } finally {
                _estaCargando.value = false
            }
        }
    }
}