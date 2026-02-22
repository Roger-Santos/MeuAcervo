package com.rogersantos.meuacervo.data.network

import com.rogersantos.meuacervo.data.model.CrossRefResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CrossRefService {
    // Buscar por DOI
    @GET("works/{doi}")
    suspend fun getWork(@Path("doi") doi: String): Response<CrossRefResponse>

    // Buscar por ISBN
    @GET("works")
    suspend fun buscarPorIsbn(
        @Query("filter") filter: String // exemplo: "isbn:9788535914849"
    ): Response<CrossRefResponse>
}