package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lugares")
data class LugarEntity(
    @PrimaryKey(autoGenerate = true) val id_lugar: Int = 0,
    val nombre_lugar: String,
    val latitud: Double,
    val longitud: Double,
    val radio_activacion: Int
)