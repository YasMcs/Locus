package com.starcode.locus.data.database
import com.starcode.locus.data.entities.UsuarioEntity
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.FichaEntity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.starcode.locus.data.dao.LocusDao

@Database(
    entities = [LugarEntity::class, UsuarioEntity::class, FichaEntity::class],
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}