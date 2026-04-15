package com.starcode.locus.ui.viewmodels

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.*
import com.starcode.locus.data.remote.request.*
import com.starcode.locus.ui.services.GeofenceBroadcastReceiver
import com.starcode.locus.ui.services.LocusFirebaseService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class MapaViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    // --- CLIENTES DE SERVICIOS ---
    private val geofencingClient = LocationServices.getGeofencingClient(application)
    private val sessionManager = SessionManager(application)

    // --- ESTADOS DE FLUJO ---
    private val _statusVisita = MutableStateFlow<String?>(null)
    val statusVisita: StateFlow<String?> = _statusVisita

    private val _statusPasos = MutableStateFlow("👣 Pasos: 0 | 📏 Distancia: 0m")
    val statusPasos: StateFlow<String> = _statusPasos

    private val _debugStep = MutableStateFlow("Esperando...")
    val debugStep: StateFlow<String> = _debugStep

    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val _favoritosIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoritosIds: StateFlow<Set<Int>> = _favoritosIds

    private val _lugarSeleccionado = MutableStateFlow<LugarEntity?>(null)

    private val _eventos = MutableSharedFlow<MapaEvent>()
    val eventos = _eventos.asSharedFlow()

    // --- VARIABLES DE RASTREO GPS ---
    private var usuarioLat = 0.0
    private var usuarioLon = 0.0
    private var ultimaNotificacionEnviada = ""
    private var ultimaLat: Double? = null
    private var ultimaLon: Double? = null
    private var distanciaAcumuladaMetros = 0.0
    private val UMBRAL_ENVIO_METROS = 50.0

    // --- PENDING INTENT PARA GEOFENCING ---
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(application, GeofenceBroadcastReceiver::class.java)
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(application, 0, intent, flags)
    }

    init {
        cargarLugares()
    }

    sealed class MapaEvent {
        object FotoGuardadaExito : MapaEvent()
        data class Error(val mensaje: String) : MapaEvent()
    }

    // --- LÓGICA DE GEOFENCING (50 METROS) ---

    fun activarGeocercas(listaLugares: List<LugarEntity>) {
        if (listaLugares.isEmpty()) return

        val geofenceList = listaLugares.map { lugar ->
            Geofence.Builder()
                .setRequestId(lugar.titulo_ficha ?: "Lugar_${lugar.id_lugar}")
                .setCircularRegion(
                    lugar.latitud,
                    lugar.longitud,
                    5f // Rango
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setNotificationResponsiveness(1000)
                .build()
        }

        val request = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()

        try {
            geofencingClient.addGeofences(request, geofencePendingIntent).run {
                addOnSuccessListener { Log.d("LocusDebug", "✅ Geocercas de 50m activadas") }
                addOnFailureListener { Log.e("LocusDebug", "❌ Error al activar geocercas: ${it.message}") }
            }
        } catch (e: SecurityException) {
            Log.e("LocusDebug", "Faltan permisos de ubicación en segundo plano")
        }
    }

    // --- LÓGICA DE LUGARES Y SINCRONIZACIÓN ---

    fun cargarLugares() {
        viewModelScope.launch {
            try {
                val dbLugares = dao.obtenerLugares()
                if (dbLugares.isEmpty()) {
                    sincronizarConServidor()
                } else {
                    _lugares.value = dbLugares
                    _favoritosIds.value = dbLugares.filter { it.isFavorite }.map { it.id_lugar }.toSet()
                    activarGeocercas(dbLugares)
                }
            } catch (e: Exception) {
                Log.e("Locus", "Error al cargar lugares: ${e.message}")
            }
        }
    }

    fun sincronizarConServidor() {
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                val token = sessionManager.obtenerToken() ?: return@launch
                val authHeader = "Bearer $token"

                val categoriasApi = RetrofitClient.instance.obtenerCategorias(authHeader)
                dao.insertarCategorias(categoriasApi)

                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares(authHeader)
                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)

                cargarFavoritosUsuario(token)

                val nuevosLugares = dao.obtenerLugares()
                _lugares.value = nuevosLugares
                activarGeocercas(nuevosLugares)
            } catch (e: Exception) {
                Log.e("LocusDebug", "Fallo en sync", e)
            } finally {
                _estaCargando.value = false
            }
        }
    }

    private suspend fun cargarFavoritosUsuario(token: String) {
        try {
            val idUsuario = sessionManager.getUserId()
            if (idUsuario > 0) {
                val favoritos = RetrofitClient.instance.obtenerFavoritosUsuario(token, idUsuario)
                val favIds = favoritos.map { it.id_lugar }.toSet()
                _favoritosIds.value = favIds
                favIds.forEach { id -> dao.actualizarEstadoFavoritoLocal(id, true) }
            }
        } catch (e: Exception) {
            Log.e("LocusDebug", "Error cargando favoritos", e)
        }
    }

    fun seleccionarLugar(lugar: LugarEntity) {
        _lugarSeleccionado.value = lugar
        registrarVisitaEnServidor(lugar)
    }

    private fun registrarVisitaEnServidor(lugar: LugarEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val idUsuario = sessionManager.getUserId()
            val token = sessionManager.obtenerToken()

            if (idUsuario <= 0 || token == null) return@launch

            try {
                // ❌ Borramos: _statusVisita.value = "⏳ Enviando visita..."
                val request = VisitaRequest(id_usuario = idUsuario, id_lugar = lugar.id_lugar)
                val response = RetrofitClient.instance.registrarVisita(request)

                if (response.isSuccessful) {
                    Log.d("LocusDebug", "✅ Visita registrada en servidor")
                    // ❌ Borramos: _statusVisita.value = "✅ Registrado: ${lugar.titulo_ficha}"
                }
            } catch (e: Exception) {
                Log.e("LocusDebug", "❌ Error al registrar visita", e)
            }
        }
    }

    fun toggleFavorito(idLugar: Int) {
        viewModelScope.launch {
            try {
                val token = sessionManager.obtenerToken() ?: return@launch
                val idUsuario = sessionManager.getUserId()
                if (idUsuario <= 0) return@launch

                val authHeader = "Bearer $token"
                val currentIds = _favoritosIds.value
                val esAhoraFavorito = idLugar !in currentIds

                _favoritosIds.value = if (!esAhoraFavorito) currentIds - idLugar else currentIds + idLugar
                dao.actualizarEstadoFavoritoLocal(idLugar, esAhoraFavorito)

                if (!esAhoraFavorito) {
                    val favorito = RetrofitClient.instance.verificarFavorito(authHeader, idUsuario, idLugar)
                    favorito?.let { RetrofitClient.instance.eliminarFavorito(authHeader, it.id_favorito) }
                } else {
                    val request = FavoritoRequest(id_usuario = idUsuario, id_lugar = idLugar)
                    RetrofitClient.instance.crearFavorito(authHeader, request)
                }

                _lugares.value = _lugares.value.map { lugar ->
                    if (lugar.id_lugar == idLugar) lugar.copy(isFavorite = esAhoraFavorito) else lugar
                }
            } catch (e: Exception) {
                cargarLugares()
            }
        }
    }

    // --- LÓGICA DE ACTIVIDAD FÍSICA Y GPS ---

    fun actualizarUbicacionReal(lat: Double, lon: Double) {
        verificarProximidadVisual(lat, lon)

        if (ultimaLat != null && ultimaLon != null) {
            val resultados = FloatArray(1)
            android.location.Location.distanceBetween(ultimaLat!!, ultimaLon!!, lat, lon, resultados)
            val distanciaPaso = resultados[0].toDouble()

            if (distanciaPaso > 2.0) {
                distanciaAcumuladaMetros += distanciaPaso
                if (distanciaAcumuladaMetros >= UMBRAL_ENVIO_METROS) {
                    registrarActividadReal(distanciaAcumuladaMetros)
                    distanciaAcumuladaMetros = 0.0
                }
            }
        }
        ultimaLat = lat
        ultimaLon = lon
    }

    private fun verificarProximidadVisual(miLat: Double, miLon: Double) {
        if (miLat == 0.0) return
        val radioDeteccionMetros = 300.0
        val puntosCercanos = _lugares.value.filter { lugar ->
            val resultados = FloatArray(1)
            android.location.Location.distanceBetween(miLat, miLon, lugar.latitud, lugar.longitud, resultados)
            resultados[0] <= radioDeteccionMetros
        }.size

        if (puntosCercanos > 0) {
            val mensajeActual = if (puntosCercanos == 1) "Tienes 1 lugar cercano." else "Tienes $puntosCercanos lugares cercanos."
            if (mensajeActual != ultimaNotificacionEnviada) {
                ultimaNotificacionEnviada = mensajeActual
                // Nota: Usamos la función estática de FirebaseService que ya corregimos
                LocusFirebaseService.mostrarNotificacion(getApplication(), "📍 ¡Locus Detectado!", mensajeActual)
            }
        } else {
            ultimaNotificacionEnviada = ""
        }
    }

    private fun registrarActividadReal(metros: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val idUsuario = sessionManager.getUserId()
            if (idUsuario <= 0) return@launch

            try {
                val pasosEstimados = (metros * 1.31).toInt()
                val request = ActividadRequest(
                    id_usuario = idUsuario,
                    distancia_metros = metros,
                    pasos = pasosEstimados,
                    duracion_segundos = 30
                )
                RetrofitClient.instance.registrarActividad(request)
            } catch (e: Exception) {
                Log.e("LocusTracking", "❌ Error sincronización actividad", e)
            }
        }
    }

    fun actualizarMonitorActividad(pasos: Int, metros: Double) {
        _statusPasos.value = "👣 Pasos: $pasos | 📏 Distancia: ${String.format("%.1f", metros)}m"
        _statusVisita.value = "¡Movimiento! +$pasos pasos"
        viewModelScope.launch {
            delay(2000)
            if (_statusVisita.value?.contains("Movimiento") == true) {
                _statusVisita.value = null
            }
        }
    }

    // --- SUBIDA DE IMÁGENES ---

    fun subirImagenConDatos(userIdBody: RequestBody, lugarIdBody: RequestBody, imagenPart: MultipartBody.Part, nota: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            _debugStep.value = "⏳ Subiendo imagen..."
            try {
                val imagenResponse = RetrofitClient.instance.subirImagen(userIdBody, lugarIdBody, imagenPart)
                val idGenerado = imagenResponse.id_imagen
                val idUsuarioInt = sessionManager.getUserId()
                val idLugarInt = _lugarSeleccionado.value?.id_lugar ?: 0

                val recuerdoReq = RecuerdoRequest(idUsuarioInt, idLugarInt, idGenerado, nota)
                RetrofitClient.instance.crearRecuerdo(recuerdoReq)

                _debugStep.value = "✅ Recuerdo guardado"
                _eventos.emit(MapaEvent.FotoGuardadaExito)
                cargarLugares()
            } catch (e: Exception) {
                _debugStep.value = "❌ Error: ${e.localizedMessage}"
                _eventos.emit(MapaEvent.Error(e.message ?: "Error desconocido"))
            } finally {
                _estaCargando.value = false
            }
        }
    }
}