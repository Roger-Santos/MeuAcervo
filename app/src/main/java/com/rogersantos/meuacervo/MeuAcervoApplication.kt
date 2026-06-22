package com.rogersantos.meuacervo

import android.app.Application
import com.rogersantos.meuacervo.data.database.LegacyDatabaseMigrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

class MeuAcervoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Activities podem dar 'await()' nisto para garantir que a migração
    // terminou antes de consultar o AppDatabase pela primeira vez.
    lateinit var migracaoConcluida: Deferred<Unit>
        private set

    override fun onCreate() {
        super.onCreate()
        migracaoConcluida = applicationScope.async {
            LegacyDatabaseMigrator.migrarSeNecessario(applicationContext)
        }
    }
}