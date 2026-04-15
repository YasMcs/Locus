package com.starcode.locus.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://locus-api-production-13fe.up.railway.app/"
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("LocusDebug", "✅ RetrofitClient.init llamado")
    }

    val instance: LocusApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 1. Interceptor de AUTH (Envía el Token)
        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            appContext?.let { context ->
                val token = SessionManager(context).obtenerToken()
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
            }
            chain.proceed(requestBuilder.build())
        }

        // 2. ✅ NUEVO: Interceptor de Respuesta (Detecta Token vencido)
        val responseInterceptor = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            if (response.code == 401) {
                Log.e("LocusDebug", "⚠️ Token vencido o inválido (401). Cerrando sesión...")

                appContext?.let { context ->
                    // Limpiamos los datos
                    SessionManager(context).cerrarSesion()

                    // Lógica de redirección (Opcional aquí, mejor en la UI, pero esto es un respaldo)
                    // Puedes disparar un Broadcast o simplemente dejar que el ViewModel falle
                }
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(responseInterceptor) // <-- Agregado aquí
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocusApiService::class.java)
    }
}