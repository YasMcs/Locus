package com.starcode.locus.util

import android.location.Location
import com.starcode.locus.data.entities.LugarEntity

fun filtrarLugaresCercanos(miLat: Double, miLon: Double, lugares: List<LugarEntity>): List<LugarEntity> {
    val resultados = FloatArray(1)
    return lugares.filter {
        Location.distanceBetween(miLat, miLon, it.latitud, it.longitud, resultados)
        resultados[0] <= 500
    }
}