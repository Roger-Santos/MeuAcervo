package com.rogersantos.meuacervo.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private fun getClient(baseUrl: String): Retrofit {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val googleBooks: GoogleBooksService by lazy {
        getClient("https://www.googleapis.com/books/v1/")
            .create(GoogleBooksService::class.java)
    }

    val openLibrary: OpenLibraryService by lazy {
        getClient("https://openlibrary.org/")
            .create(OpenLibraryService::class.java)
    }

    val openLibraryAuthor: OpenLibraryAuthorService by lazy {
        getClient("https://openlibrary.org/")
            .create(OpenLibraryAuthorService::class.java)
    }

    val crossRef: CrossRefService by lazy {
        getClient("https://api.crossref.org/")
            .create(CrossRefService::class.java)
    }

    val crossRefIssn: CrossRefIssnService by lazy {
        getClient("https://api.crossref.org/")
            .create(CrossRefIssnService::class.java)
    }
}