package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.CategoriaEntity
import com.starcode.locus.data.remote.request.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface LocusApiService {

    // --- CATEGORÍAS ---
    @GET("api/categorias")
    suspend fun obtenerCategorias(
        @Header("Authorization") token: String
    ): List<CategoriaEntity>

    // --- LUGARES ---
    @GET("api/lugares")
    suspend fun obtenerTodosLosLugares(
        @Header("Authorization") token: String
    ): List<LugarEntity>

    @GET("api/lugares/{id}")
    suspend fun obtenerLugarPorId(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): LugarEntity

    // --- IMÁGENES (Nuevas rutas para PostgreSQL) --
    // 1. Sube la imagen y recibe el ID (Paso 1 y 2)
    @Multipart
    @POST("api/imagenes/subir")
    suspend fun subirImagen(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("id_lugar") idLugar: RequestBody,
        @Part imagen: MultipartBody.Part
    ): ImagenResponse // Este objeto debe traer el id_imagen

    // 2. Crea el recuerdo usando el id_imagen recibido (Paso 3)
    @POST("api/recuerdos")
    suspend fun crearRecuerdo(
        @Body recuerdoRequest: RecuerdoRequest
    ): RecuerdoResponse

    @GET("api/imagenes/usuario/{id}")
    suspend fun obtenerImagenesUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<ImagenResponse>

    @DELETE("api/imagenes/{id}")
    suspend fun eliminarImagen(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>


    @GET("api/recuerdos/usuario/{id}")
    suspend fun obtenerRecuerdosUsuario(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<RecuerdoResponse>

    @DELETE("api/recuerdos/{id}")
    suspend fun eliminarRecuerdo(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // --- AUTH ---
    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse
}