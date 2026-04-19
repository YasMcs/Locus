package com.starcode.locusapp.data.entities

// Solo la data class, sin el "class VisitaEntity" rodeándola
data class VisitaHistorialDTO(
    val id: Int,
    val titulo_ficha: String?,
    val nombre_lugar: String?,
    val fecha_visita: String?,
    val descripcion: String?
)