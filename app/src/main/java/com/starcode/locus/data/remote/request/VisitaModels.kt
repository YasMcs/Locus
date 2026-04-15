package com.starcode.locus.data.remote.request

data class VisitaRequest(
    val id_usuario: Int,
    val id_lugar: Int
)

data class VisitaResponse(
    val id_visita: Int,
    val id_usuario: Int,
    val id_lugar: Int,
    val fecha_visita: String
)