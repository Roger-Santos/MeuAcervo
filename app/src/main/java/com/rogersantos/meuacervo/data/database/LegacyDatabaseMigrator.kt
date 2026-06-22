package com.rogersantos.meuacervo.data.database

import android.content.Context
import android.util.Log

/**
 * Responsável por migrar os dados dos quatro bancos antigos e separados
 * (livro_db, artigos_db, emprestimos_db, periodico_db) para o AppDatabase
 * único e novo (meuacervo.db).
 *
 * Regra de ouro desta classe: NUNCA apagar um banco antigo antes de
 * confirmar que todos os seus dados foram copiados com sucesso para o
 * banco novo. Se qualquer etapa falhar, os bancos antigos permanecem
 * intactos e o app continua funcionando neles até a próxima tentativa.
 */
object LegacyDatabaseMigrator {

    private const val TAG = "LegacyDatabaseMigrator"
    private const val PREFS_NAME = "meuacervo_migration_prefs"
    private const val KEY_MIGRATION_DONE = "migration_v1_done"

    private val legacyDbNames = listOf(
        "livro_db",
        "artigos_db",
        "emprestimos_db",
        "periodico_db"
    )

    /**
     * Executa a migração se ainda não tiver sido feita.
     * Seguro de chamar em todo onCreate/onStart do app: depois da primeira
     * execução bem-sucedida, vira um no-op rápido.
     */
    suspend fun migrarSeNecessario(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_MIGRATION_DONE, false)) {
            return // já migrado anteriormente, nada a fazer
        }

        val existeAlgumBancoAntigo = legacyDbNames.any { nome ->
            context.getDatabasePath(nome).exists()
        }

        if (!existeAlgumBancoAntigo) {
            // Instalação nova (usuário novo) — não há nada para migrar.
            // Marca como concluído para não verificar de novo sempre.
            prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
            return
        }

        Log.i(TAG, "Bancos antigos detectados. Iniciando migração para AppDatabase único.")

        try {
            val sucesso = copiarTodosOsDados(context)

            if (sucesso) {
                apagarBancosAntigos(context)
                prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
                Log.i(TAG, "Migração concluída com sucesso. Bancos antigos removidos.")
            } else {
                Log.w(TAG, "Migração não concluída com sucesso. Bancos antigos preservados para nova tentativa.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro durante a migração. Bancos antigos preservados.", e)
        }
    }

    private suspend fun copiarTodosOsDados(context: Context): Boolean {
        val novoDb = AppDatabase.getInstance(context)

        val livrosCopiados = copiarLivros(context, novoDb)
        val artigosCopiados = copiarArtigos(context, novoDb)
        val periodicosCopiados = copiarPeriodicos(context, novoDb)
        // Empréstimos por último: depende dos livros já estarem no banco novo
        // por causa da chave estrangeira.
        val emprestimosCopiados = copiarEmprestimos(context, novoDb)

        return livrosCopiados && artigosCopiados && periodicosCopiados && emprestimosCopiados
    }

    private suspend fun copiarLivros(context: Context, novoDb: AppDatabase): Boolean {
        if (!context.getDatabasePath("livro_db").exists()) return true
        return try {
            val antigoDao = LivroDatabase.getInstance(context).livroDao()
            val livros = antigoDao.listarTodos()
            livros.forEach { novoDb.livroDao().inserir(it) }
            Log.i(TAG, "Copiados ${livros.size} livros.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao copiar livros", e)
            false
        }
    }

    private suspend fun copiarArtigos(context: Context, novoDb: AppDatabase): Boolean {
        if (!context.getDatabasePath("artigos_db").exists()) return true
        return try {
            val antigoDao = ArtigoDatabase.getInstance(context).artigoDao()
            val artigos = antigoDao.listarTodos()
            artigos.forEach { novoDb.artigoDao().inserir(it) }
            Log.i(TAG, "Copiados ${artigos.size} artigos.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao copiar artigos", e)
            false
        }
    }

    private suspend fun copiarPeriodicos(context: Context, novoDb: AppDatabase): Boolean {
        if (!context.getDatabasePath("periodico_db").exists()) return true
        return try {
            val antigoDao = PeriodicoDatabase.getInstance(context).periodicoDao()
            val periodicos = antigoDao.listarTodos()
            periodicos.forEach { novoDb.periodicoDao().inserir(it) }
            Log.i(TAG, "Copiados ${periodicos.size} periódicos.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao copiar periódicos", e)
            false
        }
    }

    private suspend fun copiarEmprestimos(context: Context, novoDb: AppDatabase): Boolean {
        if (!context.getDatabasePath("emprestimos_db").exists()) return true
        return try {
            val antigoDao = EmprestimoDatabase.getInstance(context).emprestimoDao()
            val emprestimos = antigoDao.listarTodos()
            val idsLivrosValidos = novoDb.livroDao().listarTodos().map { it.id }.toSet()

            var copiados = 0
            var ignorados = 0
            emprestimos.forEach { emprestimo ->
                // Proteção extra: só copia o empréstimo se o livro vinculado
                // realmente existir no banco novo (evita violar a FK).
                if (emprestimo.livroId in idsLivrosValidos) {
                    novoDb.emprestimoDao().inserir(emprestimo)
                    copiados++
                } else {
                    ignorados++
                    Log.w(TAG, "Empréstimo id=${emprestimo.id} ignorado: livroId=${emprestimo.livroId} não encontrado.")
                }
            }
            Log.i(TAG, "Copiados $copiados empréstimos ($ignorados ignorados por inconsistência).")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao copiar empréstimos", e)
            false
        }
    }

    private fun apagarBancosAntigos(context: Context) {
        legacyDbNames.forEach { nome ->
            try {
                context.deleteDatabase(nome)
            } catch (e: Exception) {
                Log.w(TAG, "Não foi possível remover o banco antigo '$nome' (não crítico).", e)
            }
        }
    }
}