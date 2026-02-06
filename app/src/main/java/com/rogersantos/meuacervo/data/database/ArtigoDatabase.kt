package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.ArtigoDao
import com.rogersantos.meuacervo.data.model.Artigo

@Database(
    entities = [Artigo::class],
    version = 1,
    exportSchema = false
)
abstract class ArtigoDatabase : RoomDatabase() {

    abstract fun artigoDao(): ArtigoDao

    companion object {
        @Volatile
        private var INSTANCE: ArtigoDatabase? = null

        fun getInstance(context: Context): ArtigoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArtigoDatabase::class.java,
                    "artigos_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}