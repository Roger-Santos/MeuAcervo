package com.rogersantos.meuacervo.data.network

import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenLibraryAuthorService {
    @GET("authors/{id}.json")
    suspend fun getAuthor(@Path("id") id: String): Response<OpenLibraryAuthorDetail>
}

data class OpenLibraryAuthorDetail(
    @Json(name = "key") val key: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "birth_date") val birthDate: String? = null,
    @Json(name = "death_date") val deathDate: String? = null
)