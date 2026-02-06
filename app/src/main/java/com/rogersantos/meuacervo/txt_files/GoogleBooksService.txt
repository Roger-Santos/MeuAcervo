package com.rogersantos.meuacervo.data.network

import com.rogersantos.meuacervo.data.model.VolumesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("volumes")
    suspend fun volumesByIsbn(
        @Query("q") query: String // exemplo: "isbn:9788535914849"
    ): Response<VolumesResponse>
}