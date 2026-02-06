package com.rogersantos.meuacervo.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend fun <T> safeApiCall(
    tag: String,
    call: suspend () -> Response<T>,
    onSuccess: suspend (T) -> Unit,
    onError: (String) -> Unit = { msg -> Log.e(tag, msg) }
) {
    try {
        val response = withContext(Dispatchers.IO) { call() }

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                logApiResponse(tag, body)
                onSuccess(body)
            } else {
                onError("@string/msg_resposta_sem_corpo ($tag)")
            }
        } else {
            onError("@string/msg_erro_resposta")
        }
    } catch (e: Exception) {
        onError("@string/msg_excecao_api $tag: ${e.message}")
    }
}