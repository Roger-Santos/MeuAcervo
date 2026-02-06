package com.rogersantos.meuacervo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emprestimos")
data class Emprestimo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val livroId: Int,                        // 👈 vínculo com Livro
    val pessoa: String,
    val telefone: String?,
    val dataEmprestimo: String,
    val dataPrevistaDevolucao: String?,
    val devolvido: Boolean = false,
    val dataDevolucao: String? = null,
    val lembreteWorkId: String? = null
)