package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.CategoriaEntity
import com.starcode.locus.data.entities.VisitaHistorialDTO
import com.starcode.locus.data.remote.request.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface LocusApiService {

    // --- CATEGORÍAS ---
    @GET("api/categorias")
    suspend fun obtenerCategorias(@Header("Authorization") token: String): List<CategoriaEntity>

    // --- LUGARES ---
    @GET("api/lugares")
    suspend fun obtenerTodosLosLugares(@Header("Authorization") token: String): List<LugarEntity>

    @GET("api/lugares/{id}")
    suspend fun obtenerLugarPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): LugarEntity

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
    suspend fun obtenerImagenesUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<ImagenResponse>

    // --- VISITAS (Check-in automático) ---
    @POST("api/visitas")
    suspend fun registrarVisita(
        @Body request: VisitaRequest
    ): Response<Unit>

    // ✅ NUEVO: Obtener la lista de lugares que el usuario ha visitado
    @GET("api/usuarios/{id}/lugares-visitados")
    suspend fun obtenerLugaresVisitados(
        @Header("Authorization") token: String, // Agregado para consistencia
        @Path("id") id: Int
    ): List<VisitaHistorialDTO>

    @GET("api/visitas/usuario/{id}")
    suspend fun obtenerVisitasUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<VisitaHistorialDTO>

    // --- ACTIVIDADES FÍSICAS Y ESTADÍSTICAS ---
    @POST("api/actividades")
    suspend fun registrarActividad(
        @Body request: ActividadRequest
    ): Response<Unit>

    @GET("api/usuarios/{id}/estadisticas-totales")
    suspend fun obtenerEstadisticasTotales(
        @Path("id") id: Int,
        @Header("Authorization") token: String // ✅ DEBE tener el @Header
    ): EstadisticaResponse

    // --- AUTH (Estos no suelen llevar token porque es para entrar) ---
    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

    // --- FAVORITOS ---
    @GET("api/favoritos/usuario/{id}")
    suspend fun obtenerFavoritosUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<FavoritoResponse>

    @GET("api/favoritos/verificar")
    suspend fun verificarFavorito(
        @Header("Authorization") token: String,
        @Query("id_usuario") idUsuario: Int,
        @Query("id_lugar") idLugar: Int
    ): FavoritoResponse?

    @POST("api/favoritos")
    suspend fun crearFavorito(
        @Header("Authorization") token: String,
        @Body request: FavoritoRequest
    ): FavoritoResponse

    @DELETE("api/favoritos/{id}")
    suspend fun eliminarFavorito(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
}