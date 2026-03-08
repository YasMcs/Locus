package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.CategoriaEntity
import com.starcode.locus.data.remote.request.AuthResponse
import com.starcode.locus.data.remote.request.LoginRequest
import com.starcode.locus.data.remote.request.RegisterRequest
import retrofit2.http.*

interface LocusApiService {

    // --- CATEGORÍAS (¡La pieza que faltaba!) ---
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

    @GET("api/lugares/cercanos")
    suspend fun obtenerLugaresCercanos(
        @Header("Authorization") token: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radio") radio: Int
    ): List<LugarEntity>

    @POST("api/lugares")
    suspend fun crearLugar(
        @Header("Authorization") token: String,
        @Body lugar: LugarEntity
    ): LugarEntity

    @PUT("api/lugares/{id}")
    suspend fun actualizarLugar(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body lugar: LugarEntity
    ): LugarEntity

    @DELETE("api/lugares/{id}")
    suspend fun eliminarLugar(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Unit

    // --- AUTH ---
    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse
}