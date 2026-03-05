package com.starcode.locus.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Si usas el emulador de Android y tu servidor es local (XAMPP), usa esta IP:
    // Si usas tu teléfono real, debes poner la IP de tu computadora (ej: 192.168.1.15)
    private const val BASE_URL = "http://10.0.2.2/locus_api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}