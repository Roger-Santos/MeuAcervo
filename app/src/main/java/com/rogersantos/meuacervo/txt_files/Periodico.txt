package com.rogersantos.meuacervo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "periodicos")
data class Periodico(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val issn: String? = null
)