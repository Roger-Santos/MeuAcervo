package com.rogersantos.meuacervo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rogersantos.meuacervo.data.dao.PeriodicoDao
import com.rogersantos.meuacervo.data.model.Periodico

@Database(entities = [Periodico::class], version = 1, exportSchema = false)
abstract class PeriodicoDatabase : RoomDatabase() {

    abstract fun periodicoDao(): PeriodicoDao

    companion object {
        @Volatile
        private var INSTANCE: PeriodicoDatabase? = null

        fun getInstance(context: Context): PeriodicoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PeriodicoDatabase::class.java,
                    "periodico_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}