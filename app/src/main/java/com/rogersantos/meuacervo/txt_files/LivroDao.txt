package com.rogersantos.meuacervo.data.dao

import androidx.room.*
import com.rogersantos.meuacervo.data.model.Livro

@Dao
interface LivroDao {

    @Insert
    suspend fun inserir(livro: Livro)

    @Query("SELECT * FROM livros ORDER BY titulo ASC")
    suspend fun listarTodos(): List<Livro>

    @Query("SELECT * FROM livros WHERE id = :id")
    suspend fun buscarPorId(id: Int): Livro?

    @Delete
    suspend fun deletar(livro: Livro)

    @Update
    suspend fun atualizar(livro: Livro)
}