package com.rogersantos.meuacervo.data.model

import com.squareup.moshi.Json

data class VolumesResponse(
    @Json(name = "totalItems") val totalItems: Int = 0,
    @Json(name = "items") val items: List<VolumeItem>? = null
)

data class VolumeItem(
    @Json(name = "id") val id: String?,
    @Json(name = "volumeInfo") val volumeInfo: VolumeInfo?
)

data class VolumeInfo(
    @Json(name = "title") val title: String? = null,
    @Json(name = "authors") val authors: List<String>? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "publishedDate") val publishedDate: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "imageLinks") val imageLinks: ImageLinks? = null,
    @Json(name = "categories") val categories: List<String>? = null,
    @Json(name = "pageCount") val pageCount: Int? = null   // <-- novo campo
)

data class ImageLinks(
    @Json(name = "smallThumbnail") val smallThumbnail: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null
)