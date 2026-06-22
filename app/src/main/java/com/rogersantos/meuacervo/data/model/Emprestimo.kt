package com.rogersantos.meuacervo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emprestimos",
    foreignKeys = [
        ForeignKey(
            entity = Livro::class,
            parentColumns = ["id"],
            childColumns = ["livroId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("livroId")]
)
data class Emprestimo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val livroId: Int,                        // 👈 vínculo real com Livro, garantido pelo banco
    val pessoa: String,
    val telefone: String?,
    val dataEmprestimo: String,
    val dataPrevistaDevolucao: String?,
    val devolvido: Boolean = false,
    val dataDevolucao: String? = null,
    val lembreteWorkId: String? = null
)