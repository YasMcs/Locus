package com.starcode.locus.data.remote

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://locus-api-production.up.railway.app/"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("LocusDebug", "✅ RetrofitClient.init llamado")

        // ✅ Verificar si ya hay token guardado
        val token = SessionManager(context.applicationContext).obtenerToken()
        Log.d("LocusDebug", "🔑 Token al iniciar: ${if (token != null) "EXISTE (${token.take(20)}...)" else "NULL - usuario no logueado"}")
    }

    val instance: LocusApiService
        get() {
            val context = appContext

            val interceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                if (context != null) {
                    val token = SessionManager(context).obtenerToken()
                    Log.d("LocusDebug", "🔑 Token en interceptor: ${if (token != null) "ENVIANDO (${token.take(20)}...)" else "NULL - sin token"}")

                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                } else {
                    Log.e("LocusDebug", "❌ appContext es NULL en interceptor")
                }

                chain.proceed(requestBuilder.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(LocusApiService::class.java)
        }
}