package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.EmprestimoDao
import com.rogersantos.meuacervo.data.model.Emprestimo

// Inclui a entidade Emprestimo e o DAO correspondente
@Database(entities = [Emprestimo::class], version = 1)
abstract class EmprestimoDatabase : RoomDatabase() {

    abstract fun emprestimoDao(): EmprestimoDao

    companion object {
        @Volatile
        private var INSTANCE: EmprestimoDatabase? = null

        fun getInstance(context: Context): EmprestimoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EmprestimoDatabase::class.java,
                    "emprestimos_db"
                )
                    .fallbackToDestructiveMigration() // recria se versão mudar
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}