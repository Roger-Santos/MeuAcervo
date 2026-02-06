package com.rogersantos.meuacervo.ui.estante

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.rogersantos.meuacervo.ui.detalhes.DetalhesLivroActivity
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.LivroDatabase
import com.rogersantos.meuacervo.data.model.Livro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EstanteLivrosFragment : Fragment(R.layout.fragment_estante_lista) {
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LivroAdapter
    private lateinit var empty: TextView
    private lateinit var search: TextInputEditText
    private var listaCompleta: List<Livro> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler)
        empty = view.findViewById(R.id.txtEmpty)
        search = view.findViewById(R.id.etSearch)

        recycler.layoutManager = GridLayoutManager(requireContext(), 1)
        adapter = LivroAdapter { livro ->
            val intent = Intent(requireContext(), DetalhesLivroActivity::class.java)
            intent.putExtra("livroId", livro.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // Adiciona espaçamento entre os cards
        recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.bottom = 16   // margem inferior entre cards
                outRect.left = 8      // margem lateral
                outRect.right = 8
            }
        })

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filtrar(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        carregarLivros()
    }

    override fun onResume() {
        super.onResume()
        carregarLivros()
    }

    private fun carregarLivros() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dao = LivroDatabase.getInstance(requireContext()).livroDao()
            val livros = withContext(Dispatchers.IO) { dao.listarTodos() }
            listaCompleta = livros
            filtrar(search.text?.toString().orEmpty())
        }
    }

    private fun filtrar(query: String) {
        val filtrados = if (query.isBlank()) listaCompleta else {
            listaCompleta.filter {
                it.titulo.contains(query, true) ||
                        it.autores?.contains(query, true) == true ||
                        it.categoria?.contains(query, true) == true
            }
        }
        adapter.submitList(filtrados)
        empty.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }
}