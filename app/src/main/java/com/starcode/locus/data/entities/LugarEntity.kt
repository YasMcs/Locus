package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
@Entity(
    tableName = "lugares",
    foreignKeys = [
        ForeignKey(entity = CategoriaEntity::class, parentColumns = ["id_categoria"], childColumns = ["id_categoria"])
    ]
)
data class LugarEntity(
    @PrimaryKey(autoGenerate = true) val id_lugar: Int = 0,
    val id_categoria: Int,
    val nombre_lugar: String,
    val latitud: Double,
    val longitud: Double,
    val radio_activacion: Int,
    val titulo_ficha: String,
    val descripcion_hist: String,
    val dato_curioso: String
)