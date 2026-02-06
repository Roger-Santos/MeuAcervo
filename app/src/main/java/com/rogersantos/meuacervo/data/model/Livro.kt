package com.rogersantos.meuacervo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "livros")
data class Livro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val categoria: String? = null,          // agora padronizado
    val autores: String? = null,            // vários autores separados por vírgula
    val editora: String? = null,
    val dataPublicacao: String? = null,     // YYYY-MM-DD ou YYYY
    val paginas: Int? = null,               // número de páginas
    val descricao: String? = null,
    val urlCapa: String? = null,            // URL remota da capa
    val capaPath: String? = null,           // caminho local salvo da capa
    val palavrasChave: String? = null,      // texto com palavras-chave separado por vírgulas
    val nota: Int? = null,                  // classificação de 1 a 5
    val dataAquisicao: String? = null,      // formato DD/MM/YYYY
    val jaLido: Boolean = false
)