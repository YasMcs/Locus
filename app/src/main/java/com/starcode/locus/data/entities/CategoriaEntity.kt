package com.starcode.locus.data.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) val id_categoria: Int = 0,
    val nombre_categoria: String
)