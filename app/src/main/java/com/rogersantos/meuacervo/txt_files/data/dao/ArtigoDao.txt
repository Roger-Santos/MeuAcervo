package com.rogersantos.meuacervo.data.dao

import androidx.room.*
import com.rogersantos.meuacervo.data.model.Artigo

@Dao
interface ArtigoDao {
    @Insert
    suspend fun inserir(artigo: Artigo)

    @Update
    suspend fun atualizar(artigo: Artigo)

    @Delete
    suspend fun deletar(artigo: Artigo)

    @Query("SELECT * FROM artigos ORDER BY titulo ASC")
    suspend fun listarTodos(): List<Artigo>

    @Query("SELECT * FROM artigos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Artigo?
}