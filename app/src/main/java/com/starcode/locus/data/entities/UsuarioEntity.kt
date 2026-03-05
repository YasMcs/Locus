package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = false) // El ID viene del servidor tras el Login/Registro
    val id_usuario: Int,
    val nombre: String,
    val ape_pa: String,
    val ape_ma: String?, // Nuleable como en tu back
    val email: String,
    val password: String, // Solo la guardamos localmente si necesitas persistir la sesión
    val fecha_nac: String? = null
)