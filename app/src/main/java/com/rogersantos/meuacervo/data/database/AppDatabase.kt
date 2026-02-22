package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.dao.EmprestimoDao
import com.rogersantos.meuacervo.data.dao.ArtigoDao
import com.rogersantos.meuacervo.data.dao.PeriodicoDao
import com.rogersantos.meuacervo.data.model.Livro
import com.rogersantos.meuacervo.data.model.Emprestimo
import com.rogersantos.meuacervo.data.model.Artigo
import com.rogersantos.meuacervo.data.model.Periodico

@Database(
    entities = [
        Livro::class,
        Emprestimo::class,
        Artigo::class,
        Periodico::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun livroDao(): LivroDao
    abstract fun emprestimoDao(): EmprestimoDao
    abstract fun artigoDao(): ArtigoDao
    abstract fun periodicoDao(): PeriodicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meuacervo_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}