package com.starcode.locus.data.remote.request

import kotlinx.serialization.Serializable

// Lo que recibes de la API de imágenes
@Serializable
data class ImagenResponse(
    val id_imagen: Int, // <-- Asegúrate de que el nombre sea EXACTAMENTE este
    val url_imagen: String
)


@Serializable
data class RecuerdoRequest(
    val id_usuario: Int,
    val id_lugar: Int,
    val id_imagen: Int? = null,
    val nota: String? = null
)

// Lo que recibes al crear el recuerdo
data class RecuerdoResponse(
    val id: Int,
    val mensaje: String
)