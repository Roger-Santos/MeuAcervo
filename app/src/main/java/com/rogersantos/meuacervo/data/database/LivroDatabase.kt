package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.model.Livro

@Database(entities = [Livro::class], version = 1)
abstract class LivroDatabase : RoomDatabase() {
    abstract fun livroDao(): LivroDao

    companion object {
        @Volatile
        private var INSTANCE: LivroDatabase? = null

        fun getInstance(context: Context): LivroDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LivroDatabase::class.java,
                    "livro_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}