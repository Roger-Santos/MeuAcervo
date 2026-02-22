package com.rogersantos.meuacervo.util

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun logApiResponse(tag: String, body: Any?) {
    if (body == null) {
        Log.d(tag, "Resposta nula")
        return
    }
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(Any::class.java).serializeNulls()
    val json = adapter.toJson(body)
    Log.d(tag, "Resposta completa: $json")
}