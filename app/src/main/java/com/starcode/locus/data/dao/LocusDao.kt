package com.starcode.locus.data.dao

import androidx.room.*
import com.starcode.locus.data.entities.*


@Dao
interface LocusDao {

    // --- USUARIOS ---
    // Cambiamos a REPLACE para que si el usuario actualiza su perfil en el back, se refleje aquí
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarUsuarioPorEmail(email: String): UsuarioEntity?

    // --- LUGARES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Esto pisa el dato viejo y evita el error
    suspend fun insertarLugares(lugares: List<LugarEntity>)

    @Query("SELECT * FROM lugares")
    suspend fun obtenerLugares(): List<LugarEntity>

    // --- CATEGORÍAS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategorias(categorias: List<CategoriaEntity>)

    @Query("SELECT * FROM categorias")
    suspend fun obtenerCategorias(): List<CategoriaEntity>

    // --- HISTORIAL ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarVisita(historial: HistorialEntity)

    // Usamos un Flow para que si el historial cambia, la UI se actualice solita
    @Query("SELECT * FROM historial_lugares WHERE id_usuario = :usuarioId")
    fun obtenerHistorialUsuario(usuarioId: Int): kotlinx.coroutines.flow.Flow<List<HistorialEntity>>

    // --- INTERESES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarIntereses(intereses: List<InteresEntity>)

    @Query("DELETE FROM lugares")
    suspend fun borrarTodosLosLugares()

}
