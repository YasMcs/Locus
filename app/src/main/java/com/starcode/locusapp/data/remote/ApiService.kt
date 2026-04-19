package com.starcode.locusapp.data.remote

import com.starcode.locusapp.data.entities.LugarEntity
import com.starcode.locusapp.data.entities.CategoriaEntity
import com.starcode.locusapp.data.entities.VisitaHistorialDTO
import com.starcode.locusapp.data.remote.request.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface LocusApiService {

    @GET("api/categorias")
    suspend fun obtenerCategorias(@Header("Authorization") token: String): List<CategoriaEntity>

    @GET("api/lugares")
    suspend fun obtenerTodosLosLugares(@Header("Authorization") token: String): List<LugarEntity>

    @GET("api/lugares/{id}")
    suspend fun obtenerLugarPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): LugarEntity

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

    // --- VISITAS ---
    // Quitamos el header manual porque RetrofitClient ya lo inyecta vía Interceptor
    @POST("api/visitas")
    suspend fun registrarVisita(
        @Body request: VisitaRequest
    ): Response<Unit>

    @GET("api/usuarios/{id}/lugares-visitados")
    suspend fun obtenerLugaresVisitados(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<VisitaHistorialDTO>

    @GET("api/visitas/usuario/{id}")
    suspend fun obtenerVisitasUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<VisitaHistorialDTO>

    @POST("api/actividades")
    suspend fun registrarActividad(
        @Body request: ActividadRequest
    ): Response<Unit>

    @GET("api/usuarios/{id}/estadisticas-totales")
    suspend fun obtenerEstadisticasTotales(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): EstadisticaResponse

    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>

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