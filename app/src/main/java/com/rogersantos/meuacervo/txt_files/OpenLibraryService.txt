package com.rogersantos.meuacervo.data.network

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface OpenLibraryService {
    @GET("isbn/{isbn}.json")
    suspend fun buscarPorIsbn(@Path("isbn") isbn: String): Response<OpenLibraryResponse>
}

data class OpenLibraryResponse(
    @Json(name = "title") val title: String?,
    @Json(name = "publish_date") val publishDate: String?,
    @Json(name = "publishers") val publishers: List<String>?,
    @Json(name = "authors") val authors: List<OpenLibraryAuthor>?,
    @Json(name = "covers") val covers: List<Int>? = null,
    @Json(name = "description") val description: Any? = null // 👈 novo campo
)

data class OpenLibraryAuthor(
    @Json(name = "key") val key: String? // Ex: "/authors/OL12345A"
)