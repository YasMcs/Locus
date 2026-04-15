package com.starcode.locus.data.remote.request

data class ActividadRequest(
    val id_usuario: Int,
    val distancia_metros: Double,
    val pasos: Int,
    val duracion_segundos: Int
)