package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.request.AuthResponse
import com.starcode.locus.data.remote.request.LoginRequest
import com.starcode.locus.data.remote.request.RegisterRequest
import retrofit2.http.*

interface LocusApiService {

    // --- SECCIÓN DE LUGARES ---

    @GET("api/lugares")
    suspend fun obtenerTodosLosLugares(): List<LugarEntity>

    @GET("api/lugares/{id}")
    suspend fun obtenerLugarPorId(@Path("id") id: Int): LugarEntity

    @GET("api/lugares/cercanos")
    suspend fun obtenerLugaresCercanos(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radio") radio: Int
    ): List<LugarEntity>

    // --- MÉTODOS DE GESTIÓN (CRUD) ---


    @POST("api/lugares")
    suspend fun crearLugar(@Body lugar: LugarEntity): LugarEntity

    @PUT("api/lugares/{id}")
    suspend fun actualizarLugar(@Path("id") id: Int, @Body lugar: LugarEntity): LugarEntity

    @DELETE("api/lugares/{id}")
    suspend fun eliminarLugar(@Path("id") id: Int): Unit

    // --- SECCIÓN DE AUTENTICACIÓN ---

    @POST("auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse
}