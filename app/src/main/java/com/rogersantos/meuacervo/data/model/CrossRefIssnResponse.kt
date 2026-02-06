package com.rogersantos.meuacervo.data.model

import com.squareup.moshi.Json

data class CrossRefIssnResponse(
    @Json(name = "status") val status: String?,
    @Json(name = "message") val message: CrossRefIssnMessage?
)

data class CrossRefIssnMessage(
    @Json(name = "publisher") val publisher: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "ISSN") val issnList: List<String>?,
    @Json(name = "counts") val counts: CrossRefIssnCounts?
)

data class CrossRefIssnCounts(
    @Json(name = "total-dois") val totalDois: Int?,
    @Json(name = "current-dois") val currentDois: Int?
)