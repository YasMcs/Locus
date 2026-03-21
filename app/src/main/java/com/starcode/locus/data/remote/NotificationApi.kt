package com.starcode.locus.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Un nombre más estándar para una API de registro
interface NotificationApi {
    @POST("register-token")
    suspend fun registrarDispositivo(
        @Body request: TokenRequest
    ): Response<Unit>
}

data class TokenRequest(
    val nombre: String,
    val token: String
)