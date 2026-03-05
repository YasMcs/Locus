package com.starcode.locus.data.remote

import com.starcode.locus.data.entities.UsuarioEntity
import com.starcode.locus.data.remote.request.AuthResponse
import com.starcode.locus.data.remote.request.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login.php") // Ajusta el nombre del archivo según tu backend
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("registro.php")
    suspend fun registrar(@Body usuario: UsuarioEntity): AuthResponse
}