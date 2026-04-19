package com.starcode.locusapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.starcode.locusapp.data.dao.LocusDao
// Importa todas tus entidades para que Room las reconozca
import com.starcode.locusapp.data.entities.UsuarioEntity
import com.starcode.locusapp.data.entities.LugarEntity
import com.starcode.locusapp.data.entities.CategoriaEntity
import com.starcode.locusapp.data.entities.HistorialEntity
import com.starcode.locusapp.data.entities.InteresEntity

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
                    "locus_v2_db"
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