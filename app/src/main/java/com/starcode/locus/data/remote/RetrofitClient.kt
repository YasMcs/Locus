package com.starcode.locus.data.remote

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://locus-api-production.up.railway.app/"

    private const val PROFESOR_URL = "http://192.168.1.86:3000/"


    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("LocusDebug", "✅ RetrofitClient.init llamado")
    }

    // 2. Usamos 'by lazy' para que el cliente se cree una sola vez de forma eficiente
    val instance: LocusApiService by lazy {

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()

            // Usamos el contexto inicializado para obtener el SessionManager
            val context = appContext
            if (context != null) {
                val token = SessionManager(context).obtenerToken()
                if (!token.isNullOrBlank()) {
                    // .header reemplaza cualquier header previo (evita duplicados)
                    requestBuilder.header("Authorization", "Bearer $token")
                    Log.d("LocusDebug", "🚀 Interceptor: Enviando Token a ${chain.request().url}")
                } else {
                    Log.w("LocusDebug", "⚠️ Interceptor: TOKEN NO ENCONTRADO")
                }
            } else {
                Log.e("LocusDebug", "❌ appContext es NULL")
            }

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocusApiService::class.java)
    }

    // Servicio dedicado a la gestión de tokens de notificación en red local
    val notificationRegistryService: NotificationApi by lazy {
        Retrofit.Builder()
            .baseUrl(PROFESOR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)
    }
}