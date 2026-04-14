package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.CategoriaEntity
import com.starcode.locus.data.remote.request.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

// --- NUEVOS DATA CLASSES PARA EL BODY ---
data class VisitaRequest(val id_usuario: Int, val id_lugar: Int)
data class VisitaResponse(val id_visita: Int, val id_usuario: Int, val id_lugar: Int, val fecha_visita: String)

data class ActividadRequest(
    val id_usuario: Int,
    val distancia_metros: Float,
    val pasos: Int,
    val duracion_segundos: Int
)

data class EstadisticasGlobalesResponse(
    val total_km: Double,
    val total_pasos: Int,
    val lugares_descubiertos: Int
)

interface LocusApiService {

    // --- CATEGORÍAS ---
    @GET("api/categorias")
    suspend fun obtenerCategorias(@Header("Authorization") token: String): List<CategoriaEntity>

    // --- LUGARES ---
    @GET("api/lugares")
    suspend fun obtenerTodosLosLugares(@Header("Authorization") token: String): List<LugarEntity>

    @GET("api/lugares/{id}")
    suspend fun obtenerLugarPorId(@Header("Authorization") token: String, @Path("id") id: Int): LugarEntity

    // --- IMÁGENES Y RECUERDOS ---
    @Multipart
    @POST("api/imagenes/subir")
    suspend fun subirImagen(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("id_lugar") idLugar: RequestBody,
        @Part imagen: MultipartBody.Part
    ): ImagenResponse

    @POST("api/recuerdos")
    suspend fun crearRecuerdo(@Body recuerdoRequest: RecuerdoRequest): RecuerdoResponse

    @GET("api/imagenes/usuario/{id}")
    suspend fun obtenerImagenesUsuario(@Header("Authorization") token: String, @Path("id") id: Int): List<ImagenResponse>

    // --- NUEVO: VISITAS (Check-in automático) ---
    @POST("api/visitas")
    suspend fun registrarVisita(
        @Header("Authorization") token: String,
        @Body request: VisitaRequest
    ): Response<Unit>

    @GET("api/visitas/usuario/{id}")
    suspend fun obtenerVisitasUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<VisitaResponse>

    // --- NUEVO: ACTIVIDADES FÍSICAS Y ESTADÍSTICAS REALES ---
    @POST("api/actividades")
    suspend fun registrarActividad(
        @Header("Authorization") token: String,
        @Body request: ActividadRequest
    ): Response<Unit>

    @GET("api/usuarios/{id}/estadisticas-totales")
    suspend fun obtenerEstadisticasTotales(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): EstadisticasGlobalesResponse

    // --- AUTH ---
    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    // --- FAVORITOS ---
    @GET("api/favoritos/usuario/{id}")
    suspend fun obtenerFavoritosUsuario(@Header("Authorization") token: String, @Path("id") id: Int): List<FavoritoResponse>

    @GET("api/favoritos/verificar")
    suspend fun verificarFavorito(
        @Header("Authorization") token: String,
        @Query("id_usuario") idUsuario: Int,
        @Query("id_lugar") idLugar: Int
    ): FavoritoResponse?

    @POST("api/favoritos")
    suspend fun crearFavorito(@Header("Authorization") token: String, @Body request: FavoritoRequest): FavoritoResponse

    @DELETE("api/favoritos/{id}")
    suspend fun eliminarFavorito(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>
}