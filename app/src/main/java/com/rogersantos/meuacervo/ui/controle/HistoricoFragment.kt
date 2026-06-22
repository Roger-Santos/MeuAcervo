package com.rogersantos.meuacervo.ui.controle

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.dao.EmprestimoDao
import com.rogersantos.meuacervo.data.dao.LivroDao
import com.rogersantos.meuacervo.data.database.AppDatabase
import com.rogersantos.meuacervo.data.model.Emprestimo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoricoFragment : Fragment(R.layout.fragment_historico) {

    private lateinit var recycler: RecyclerView
    private lateinit var empty: TextView
    private lateinit var adapter: GrupoEmprestimoAdapter
    private lateinit var emprestimoDao: EmprestimoDao
    private lateinit var livroDao: LivroDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.recyclerHistorico)
        empty = view.findViewById(R.id.txtEmptyHistorico)

        emprestimoDao = AppDatabase.getInstance(requireContext()).emprestimoDao()
        livroDao = AppDatabase.getInstance(requireContext()).livroDao()

        adapter = GrupoEmprestimoAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        carregarHistoricoAgrupado()
    }

    private fun carregarHistoricoAgrupado() {
        viewLifecycleOwner.lifecycleScope.launch {
            val grupos = withContext(Dispatchers.IO) {
                val todosEmprestimos = emprestimoDao.listarTodos()
                val agrupado = todosEmprestimos.groupBy { it.livroId }

                agrupado.map { (livroId, lista) ->
                    val livro = livroDao.buscarPorId(livroId)
                    EmprestimoGrupo(
                        livroId = livroId,
                        tituloLivro = livro?.titulo ?: getString(R.string.msg_livro_desconhecido),
                        capaLivro = livro?.capaPath ?: livro?.urlCapa,
                        emprestimos = lista.sortedByDescending { it.dataEmprestimo }
                    )
                }.sortedBy { it.tituloLivro.lowercase() }
            }

            adapter.submitList(grupos)
            empty.visibility = if (grupos.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

/** Modelo de grupo para o histórico por livro */
data class EmprestimoGrupo(
    val livroId: Int,
    val tituloLivro: String,
    val capaLivro: String?,
    val emprestimos: List<Emprestimo>
)