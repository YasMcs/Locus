package com.starcode.locus.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = false) // El ID viene de tu Back
    val id_categoria: Int,
    val nombre_categoria: String
)