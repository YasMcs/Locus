package com.starcode.locus.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fichas_informativas")
data class FichaEntity(
    @PrimaryKey(autoGenerate = true) val id_ficha: Int = 0,
    val id_lugar: Int,
    val titulo: String,
    val descripcion_hist: String,
    val dato_curioso: String
)