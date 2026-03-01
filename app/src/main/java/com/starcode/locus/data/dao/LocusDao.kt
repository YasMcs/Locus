package com.starcode.locus.data.dao

import androidx.room.*
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.UsuarioEntity

@Dao
interface LocusDao {
    //lugares
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarLugar(lugar: LugarEntity)

    @Query("SELECT * FROM lugares")
    suspend fun obtenerLugares(): List<LugarEntity>

    // usuarios
    @Insert
    suspend fun registrarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarUsuarioPorEmail(email: String): UsuarioEntity?

    // fichass
    @Query("SELECT * FROM fichas_informativas WHERE id_lugar = :lugarId")
    suspend fun obtenerFichasDeLugar(lugarId: Int): List<FichaEntity>

    @Insert
    suspend fun insertarFicha(ficha: FichaEntity)
}