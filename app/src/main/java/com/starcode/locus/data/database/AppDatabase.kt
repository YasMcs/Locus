package com.starcode.locus.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.starcode.locus.data.dao.LocusDao
// Importa todas tus entidades para que Room las reconozca
import com.starcode.locus.data.entities.UsuarioEntity
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.CategoriaEntity
import com.starcode.locus.data.entities.HistorialEntity
import com.starcode.locus.data.entities.InteresEntity

@Database(
    entities = [
        LugarEntity::class,
        UsuarioEntity::class,
        CategoriaEntity::class, // Agregada para resolver el error de Foreign Key
        HistorialEntity::class,
        InteresEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locusDao(): LocusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "locus_db"
                )
                    // Esto ayuda a manejar cambios de esquema mientras desarrollas
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}