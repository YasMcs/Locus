package com.starcode.locus.data.remote.request

// Lo que recibes de la API de imágenes
data class ImagenResponse(
    val id: Int,
    val url: String
)

// Lo que envías para crear el recuerdo
data class RecuerdoRequest(
    val usuario_id: Int,
    val lugar_id: Int,
    val imagen_id: Int
)

// Lo que recibes al crear el recuerdo
data class RecuerdoResponse(
    val id: Int,
    val mensaje: String
)