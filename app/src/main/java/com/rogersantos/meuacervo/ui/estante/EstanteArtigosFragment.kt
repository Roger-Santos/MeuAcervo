package com.rogersantos.meuacervo.ui.estante

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rogersantos.meuacervo.ui.detalhes.DetalhesArtigoActivity
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.ArtigoDatabase
import com.rogersantos.meuacervo.data.model.Artigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EstanteArtigosFragment : Fragment(R.layout.fragment_estante_lista) {
    private lateinit var recycler: RecyclerView
    private lateinit var empty: TextView
    private lateinit var search: EditText

    private lateinit var adapter: ArtigoAdapter
    private var listaCompleta: List<Artigo> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.recycler)
        empty = view.findViewById(R.id.txtEmpty)
        search = view.findViewById(R.id.etSearch)

        // ✅ LayoutManager em lista (um item por linha)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Clique abre DetalhesArtigoActivity
        adapter = ArtigoAdapter { artigo ->
            val intent = Intent(requireContext(), DetalhesArtigoActivity::class.java)
            intent.putExtra("artigoId", artigo.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        // ✅ Filtro reativo
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                filtrar(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        carregarArtigos()
    }

    override fun onResume() {
        super.onResume()
        carregarArtigos()
    }

    private fun carregarArtigos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val dao = ArtigoDatabase.getInstance(requireContext()).artigoDao()
            val artigos = withContext(Dispatchers.IO) { dao.listarTodos() }
            listaCompleta = artigos
            filtrar(search.text?.toString().orEmpty())
        }
    }

    private fun filtrar(query: String) {
        val filtrados = if (query.isBlank()) listaCompleta else {
            listaCompleta.filter {
                it.titulo.contains(query, true) ||
                        it.autores?.contains(query, true) == true ||
                        it.periodico?.contains(query, true) == true
            }
        }
        adapter.submitList(filtrados)
        empty.visibility = if (filtrados.isEmpty()) View.VISIBLE else View.GONE
    }
}