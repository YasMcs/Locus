package com.starcode.locus.data.remote.request

import kotlinx.serialization.Serializable

// Lo que recibes de la API de imágenes
@Serializable
data class ImagenResponse(
    val id_imagen: Int,
    val id_usuario: Int,
    val id_lugar: Int,
    val url_imagen: String,
    val fecha_subida: String, // El datetime de Exposed llega como String (ISO)
    val nombre_lugar: String? = "Lugar Desconocido" // <--- Agrega este si el back lo envía
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