package com.starcode.locus.data.dao

import androidx.room.*
import com.starcode.locus.data.entities.*

@Dao
interface LocusDao {

    // --- USUARIOS ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarUsuarioPorEmail(email: String): UsuarioEntity?

    // --- LUGARES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLugar(lugar: LugarEntity)

    @Query("SELECT * FROM lugares")
    suspend fun obtenerLugares(): List<LugarEntity>

    // --- CATEGORÍAS (Necesarias para evitar errores de FK) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategoria(categoria: CategoriaEntity)

    @Query("SELECT * FROM categorias")
    suspend fun obtenerCategorias(): List<CategoriaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarVisita(historial: HistorialEntity)

    @Query("SELECT * FROM historial_lugares WHERE id_usuario = :usuarioId")
    suspend fun obtenerHistorialUsuario(usuarioId: Int): List<HistorialEntity>

    // --- INTERESES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarInteres(interes: InteresEntity)
}