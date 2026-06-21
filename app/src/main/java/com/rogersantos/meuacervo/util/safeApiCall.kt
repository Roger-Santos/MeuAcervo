package com.rogersantos.meuacervo.util

import android.content.Context
import android.util.Log
import com.rogersantos.meuacervo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend fun <T> safeApiCall(
    context: Context,
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
                onError(context.getString(R.string.msg_resposta_sem_corpo, tag))
            }
        } else {
            onError(context.getString(R.string.msg_erro_resposta, response.code(), response.message()))
        }
    } catch (e: Exception) {
        onError(context.getString(R.string.msg_excecao_api, tag, e.message ?: ""))
    }
}