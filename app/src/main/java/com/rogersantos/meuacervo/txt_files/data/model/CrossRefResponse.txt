package com.rogersantos.meuacervo.data.model

import com.squareup.moshi.Json

// Resposta genérica do CrossRef (serve para DOI e ISBN)
data class CrossRefResponse(
    @Json(name = "status") val status: String?,
    @Json(name = "message") val message: CrossRefMessage?
)

// Para ISBN: message.items é uma lista
// Para DOI: message pode ser tratado como um único item (CrossRefItem)
data class CrossRefMessage(
    @Json(name = "items") val items: List<CrossRefItem>?,

    // Quando a consulta é por DOI, o objeto message já é um único item
    @Json(name = "title") val title: List<String>? = null,
    @Json(name = "author") val author: List<CrossRefAuthor>? = null,
    @Json(name = "container-title") val containerTitle: List<String>? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "published-print") val publishedPrint: CrossRefDateParts? = null,
    @Json(name = "published-online") val publishedOnline: CrossRefDateParts? = null,
    @Json(name = "page") val page: String? = null,
    @Json(name = "DOI") val doi: String? = null,
    @Json(name = "URL") val url: String? = null,
    @Json(name = "abstract") val abstractText: String? = null,
    @Json(name = "created") val created: CrossRefCreated? = null
)

// Representa um item retornado em uma lista (ISBN)
data class CrossRefItem(
    @Json(name = "title") val title: List<String>?,
    @Json(name = "author") val author: List<CrossRefAuthor>?,
    @Json(name = "publisher") val publisher: String?,
    @Json(name = "published-print") val publishedPrint: CrossRefDateParts?,
    @Json(name = "published-online") val publishedOnline: CrossRefDateParts?,
    @Json(name = "page") val page: String?,
    @Json(name = "DOI") val doi: String?,
    @Json(name = "URL") val url: String?,
    @Json(name = "abstract") val abstractText: String?,
    @Json(name = "created") val created: CrossRefCreated?
)

data class CrossRefAuthor(
    @Json(name = "given") val given: String?,
    @Json(name = "family") val family: String?
)

data class CrossRefDateParts(
    @Json(name = "date-parts") val dateParts: List<List<Int>>?
)

data class CrossRefCreated(
    @Json(name = "date-time") val dateTime: String?
)