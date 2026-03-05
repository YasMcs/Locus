package com.starcode.locus.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.167:8080/"
    private var sessionManager: SessionManager? = null

    // Función para inicializar el manager desde el MainActivity o Application
    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()

        // Si tenemos un token guardado, lo añadimos al Header
        sessionManager?.obtenerToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val instance: LocusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // <--- Aquí inyectamos el interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocusApiService::class.java)
    }
}