package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.ArtigoDao
import com.rogersantos.meuacervo.data.dao.EmprestimoDao
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.dao.PeriodicoDao
import com.rogersantos.meuacervo.data.model.Artigo
import com.rogersantos.meuacervo.data.model.Emprestimo
import com.rogersantos.meuacervo.data.model.Livro
import com.rogersantos.meuacervo.data.model.Periodico

// Nome do arquivo de banco novo e único.
// Importante: NUNCA reutilizar os nomes antigos (livro_db, artigos_db,
// emprestimos_db, periodico_db) aqui — isso evita qualquer conflito com
// arquivos que ainda existem no dispositivo do usuário durante a migração.
private const val DATABASE_NAME = "meuacervo.db"

@Database(
    entities = [Livro::class, Artigo::class, Emprestimo::class, Periodico::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun livroDao(): LivroDao
    abstract fun artigoDao(): ArtigoDao
    abstract fun emprestimoDao(): EmprestimoDao
    abstract fun periodicoDao(): PeriodicoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}