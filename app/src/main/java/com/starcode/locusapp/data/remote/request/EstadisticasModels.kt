package com.starcode.locusapp.data.remote.request

data class EstadisticaResponse(
    val total_km: Double,           // Antes km_recorridos
    val total_pasos: Int,           // Antes pasos_totales
    val lugares_descubiertos: Int   // Antes lugares_distintos
)