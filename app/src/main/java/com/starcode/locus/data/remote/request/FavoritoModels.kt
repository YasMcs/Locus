package com.starcode.locus.data.remote.request

data class FavoritoRequest(
    val id_usuario: Int,
    val id_lugar: Int
)

data class FavoritoResponse(
    val id_favorito: Int,
    val id_usuario: Int,
    val id_lugar: Int
)
