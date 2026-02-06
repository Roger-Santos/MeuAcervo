package com.rogersantos.meuacervo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artigos")
data class Artigo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val autores: String? = null,          // nomes separados por vírgula
    val periodico: String? = null,        // nome da revista/periódico
    val anoPublicacao: Int? = null,       // ano (YYYY)
    val doi: String? = null,              // identificador DOI
    val link: String? = null,             // URL de acesso
    val resumo: String? = null,           // resumo do artigo
    val palavrasChave: String? = null,    // separadas por vírgula
    val paginas: Int? = null,             // número de páginas
    val nota: Int? = null,                // avaliação (1 a 5)
    val jaLido: Boolean = false           // status de leitura
)