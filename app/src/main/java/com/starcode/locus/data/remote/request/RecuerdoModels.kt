package com.starcode.locus.data.remote.request

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.google.gson.annotations.SerializedName

@Serializable
data class ImagenResponse(
    @SerialName("id_imagen")
    @SerializedName("id_imagen")
    val id_imagen: Int,

    @SerialName("id_usuario")
    @SerializedName("id_usuario")
    val id_usuario: Int,

    @SerialName("id_lugar")
    @SerializedName("id_lugar")
    val id_lugar: Int,

    @SerialName("url_imagen")
    @SerializedName("url_imagen")
    val url_imagen: String,

    @SerialName("fecha_subida")
    @SerializedName("fecha_subida")
    val fecha_subida: String,

    @SerialName("nombre_lugar")
    @SerializedName("nombre_lugar")
    val nombre_lugar: String? = "Lugar Desconocido"
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