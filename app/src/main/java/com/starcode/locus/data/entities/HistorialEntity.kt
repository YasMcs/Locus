package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "historial_lugares",
    foreignKeys = [
        ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario"]),
        ForeignKey(entity = LugarEntity::class, parentColumns = ["id_lugar"], childColumns = ["id_lugar"])
    ]
)
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id_registro: Int = 0,
    val id_usuario: Int,
    val id_lugar: Int,
    val fecha_visita: Long = System.currentTimeMillis(),
    val es_favorito: Boolean = false
)