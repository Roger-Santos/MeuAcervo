package com.rogersantos.meuacervo.data.network

import com.rogersantos.meuacervo.data.model.CrossRefIssnResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CrossRefIssnService {
    @GET("journals/{issn}")
    suspend fun getJournal(@Path("issn") issn: String): Response<CrossRefIssnResponse>
}