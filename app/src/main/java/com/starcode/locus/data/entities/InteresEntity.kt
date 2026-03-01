package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "usuario_intereses",
    primaryKeys = ["id_usuario", "id_categoria"],
    foreignKeys = [
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"]),
        ForeignKey(entity = CategoriaEntity::class, parentColumns = ["id_categoria"], childColumns = ["id_categoria"])
    ]
)
data class InteresEntity(
    val id_usuario: Int,
    val id_categoria: Int
)