package com.rogersantos.meuacervo.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend fun buscarSequencialAny(
    chamadas: List<suspend () -> Response<out Any>>,
    processar: suspend (Any) -> Boolean
): Boolean {
    for (call in chamadas) {
        try {
            val response = withContext(Dispatchers.IO) { call() }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    logApiResponse("BuscaSequencial", body)
                    val encontrado = processar(body)
                    if (encontrado) return true // ✅ encerra se achou
                }
            } else {
                Log.w("BuscaSequencial", "Erro ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("BuscaSequencial", "Exceção: ${e.message}", e)
        }
    }
    return false
}