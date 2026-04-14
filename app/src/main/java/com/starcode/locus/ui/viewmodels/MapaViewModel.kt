package com.starcode.locus.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.request.FavoritoRequest
import com.starcode.locus.data.remote.request.FavoritoResponse
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.RecuerdoRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody
import retrofit2.Response

class MapaViewModel(application: Application, private val dao: LocusDao) : AndroidViewModel(application) {

    private val _debugStep = MutableStateFlow("Esperando...") // ✅ AGREGA ESTA
    val debugStep: StateFlow<String> = _debugStep            // ✅ AGREGA ESTA

    private val _lugares = MutableStateFlow<List<LugarEntity>>(emptyList())
    val lugares: StateFlow<List<LugarEntity>> = _lugares

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    private val _favoritosIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoritosIds: StateFlow<Set<Int>> = _favoritosIds

    private val sessionManager = SessionManager(application)
    // Canal para notificar eventos a la Vista (como el éxito de guardado)
    private val _eventos = MutableSharedFlow<MapaEvent>()
    val eventos = _eventos.asSharedFlow()
    // --- VARIABLES DE ESTADO PARA UBICACIÓN Y CONTROL DE BUCLE ---
    private var usuarioLat = 0.0
    private var usuarioLon = 0.0
    private var ultimaNotificacionEnviada = ""

    init {
        cargarLugares()
    }
    sealed class MapaEvent {
        object FotoGuardadaExito : MapaEvent()
        data class Error(val mensaje: String) : MapaEvent()
    }
    fun cargarLugares() {
        viewModelScope.launch {
            try {
                val dbLugares = dao.obtenerLugares()
                if (dbLugares.isEmpty()) {
                    sincronizarConServidor()
                } else {
                    // Si ya hay datos, los mostramos directamente
                    _lugares.value = dbLugares
                    // También actualizamos los IDs en memoria para que los corazones brillen
                    _favoritosIds.value = dbLugares.filter { it.isFavorite }.map { it.id_lugar }.toSet()
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

                // 1. Traer categorías y lugares de la API
                val categoriasApi = RetrofitClient.instance.obtenerCategorias(authHeader)
                dao.insertarCategorias(categoriasApi)

                val lugaresApi = RetrofitClient.instance.obtenerTodosLosLugares(authHeader)

                // 2. Limpiar e insertar lugares nuevos
                dao.borrarTodosLosLugares()
                dao.insertarLugares(lugaresApi)

                // 3. ¡VITAL! Cargar los favoritos de Railway y aplicarlos a la DB Local
                cargarFavoritosUsuario(token)

                // 4. Finalmente, leer de la DB local ya actualizada
                val nuevosLugares = dao.obtenerLugares()
                _lugares.value = nuevosLugares

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

                // ACTUALIZAR ROOM: Marcamos en la DB local lo que el server dice que es favorito
                favIds.forEach { id ->
                    dao.actualizarEstadoFavoritoLocal(id, true)
                }

                Log.d("LocusDebug", "Favoritos sincronizados: ${favoritos.size}")
            }
        } catch (e: Exception) {
            Log.e("LocusDebug", "Error cargando favoritos", e)
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

                // 1. ACTUALIZACIÓN OPTIMISTA (Para que el corazón cambie rápido en pantalla)
                val newIds = if (!esAhoraFavorito) currentIds - idLugar else currentIds + idLugar
                _favoritosIds.value = newIds

                // 2. GUARDAR EN LA DB LOCAL (Esto evita que se borre al cerrar la app)
                dao.actualizarEstadoFavoritoLocal(idLugar, esAhoraFavorito)

                // 3. MANDAR AL SERVIDOR (Railway)
                if (!esAhoraFavorito) {
                    // Si ya era favorito, lo borramos en el server
                    val favorito = RetrofitClient.instance.verificarFavorito(authHeader, idUsuario, idLugar)
                    favorito?.let { RetrofitClient.instance.eliminarFavorito(authHeader, it.id_favorito) }
                } else {
                    // Si no era, creamos el registro en el server
                    val request = FavoritoRequest(id_usuario = idUsuario, id_lugar = idLugar)
                    RetrofitClient.instance.crearFavorito(authHeader, request)
                }

                // 4. REFRESCAR LA LISTA EN MEMORIA
                _lugares.value = _lugares.value.map { lugar ->
                    if (lugar.id_lugar == idLugar) lugar.copy(isFavorite = esAhoraFavorito) else lugar
                }

            } catch (e: Exception) {
                Log.e("LocusDebug", "Error al guardar favorito: ${e.message}")
                // Si algo falla, recargamos para no mostrar datos falsos
                cargarLugares()
            }
        }
    }

    // ✅ RECIBE GPS DESDE LA SCREEN
    fun actualizarUbicacionReal(lat: Double, lon: Double) {
        usuarioLat = lat
        usuarioLon = lon
        // Solo verificamos si no estamos cargando datos
        if (!_estaCargando.value && _lugares.value.isNotEmpty()) {
            verificarProximidadReal(lat, lon)
        }
    }

    private fun verificarProximidadReal(miLat: Double, miLon: Double) {
        if (miLat == 0.0) return

        val radioDeteccionMetros = 300.0 // Radio para la notificación automática

        // ✅ FILTRO REAL: Solo contamos los que están cerca de tu posición ACTUAL (Casa vs Uni)
        val puntosCercanos = _lugares.value.filter { lugar ->
            val resultados = FloatArray(1)
            android.location.Location.distanceBetween(miLat, miLon, lugar.latitud, lugar.longitud, resultados)
            resultados[0] <= radioDeteccionMetros
        }.size

        // ✅ LÓGICA ANTI-BUCLE
        if (puntosCercanos > 0) {
            val mensajeActual = if (puntosCercanos == 1)
                "Tienes 1 lugar cercano por descubrir."
            else
                "Tienes $puntosCercanos lugares cercanos por descubrir."

            // Solo dispara la notificación si el mensaje cambió (evita "la plana")
            if (mensajeActual != ultimaNotificacionEnviada) {
                ultimaNotificacionEnviada = mensajeActual
                lanzarNotificacionLocal("📍 ¡Locus Detectado!", mensajeActual)
            }
        } else {
            // Si no hay nada cerca, reseteamos para que pueda volver a avisar al entrar en zona
            ultimaNotificacionEnviada = ""
        }
    }

    private fun lanzarNotificacionLocal(titulo: String, mensaje: String) {
        val context = getApplication<Application>().applicationContext
        val channelId = "locus_proximity"
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val firebaseAnalytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(context)

        val bundle = android.os.Bundle()
        bundle.putString("lugar_detectado", mensaje)
        firebaseAnalytics.logEvent("notificacion_proximidad", bundle)

        val intent = android.content.Intent(context, com.starcode.locus.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alertas Locus",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.starcode.locus.R.drawable.locuslogo)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
    // Agrega esto también arriba si no lo tienes
    private val _lugarSeleccionado = MutableStateFlow<LugarEntity?>(null)

    // Y crea una función para que cuando toques un marcador en el mapa, el ViewModel sepa cuál es
    fun seleccionarLugar(lugar: LugarEntity) {
        _lugarSeleccionado.value = lugar
    }
    fun subirImagenConDatos(userIdBody: RequestBody, lugarIdBody: RequestBody, imagenPart: MultipartBody.Part, nota: String) {
        viewModelScope.launch {
            _estaCargando.value = true
            _debugStep.value = "⏳ Paso 1: Subiendo archivo de imagen..."

            try {
                // 1. SUBIR LA IMAGEN
                // Esta respuesta DEBE traer el id_imagen que generó la base de datos
                val imagenResponse = RetrofitClient.instance.subirImagen(
                    idUsuario = userIdBody,
                    idLugar = lugarIdBody,
                    imagen = imagenPart
                )

                val idGenerado = imagenResponse.id_imagen
                _debugStep.value = "📸 Imagen subida (ID: $idGenerado). Registrando recuerdo..."

                // 2. CREAR EL RECUERDO (El vínculo oficial)
                // Extraemos los IDs del RequestBody (o pásalos como Int a la función para más fácil)
                val idUsuarioInt = sessionManager.getUserId()
                val idLugarInt = _lugarSeleccionado.value?.id_lugar ?: 0 // O como lo manejes

                val recuerdoReq = RecuerdoRequest(
                    id_usuario = idUsuarioInt,
                    id_lugar = idLugarInt,
                    id_imagen = idGenerado,
                    nota = nota
                )

                val respuestaFinal = RetrofitClient.instance.crearRecuerdo(recuerdoReq)

                _debugStep.value = "✅ ¡Recuerdo guardado con éxito! ${respuestaFinal.mensaje}"

                // 3. REFRESCAR LUGARES (Opcional)
                cargarLugares()

            } catch (e: Exception) {
                _debugStep.value = "❌ Error en el proceso: ${e.localizedMessage}"
                Log.e("LocusDebug", "Fallo total: ", e)
            } finally {
                _estaCargando.value = false
            }
        }
    }
}
