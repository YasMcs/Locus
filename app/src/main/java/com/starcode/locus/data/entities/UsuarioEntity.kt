package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String,
    val email: String,
    val password: String
)