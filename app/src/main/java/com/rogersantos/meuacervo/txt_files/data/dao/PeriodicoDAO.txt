package com.rogersantos.meuacervo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rogersantos.meuacervo.data.model.Periodico

@Dao
interface PeriodicoDao {

    @Insert
    suspend fun inserir(periodico: Periodico)

    @Update
    suspend fun atualizar(periodico: Periodico)

    @Query("SELECT * FROM periodicos ORDER BY nome ASC")
    suspend fun listarTodos(): List<Periodico>

    @Query("SELECT * FROM periodicos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Periodico?

    @Query("SELECT * FROM periodicos WHERE nome = :nome LIMIT 1")
    suspend fun buscarPorNome(nome: String): Periodico?
}