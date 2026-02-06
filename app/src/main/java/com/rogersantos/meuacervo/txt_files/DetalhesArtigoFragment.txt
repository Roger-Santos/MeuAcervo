package com.rogersantos.meuacervo.ui.detalhes

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.ArtigoDatabase
import com.rogersantos.meuacervo.data.model.Artigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesArtigoFragment : Fragment(R.layout.fragment_detalhes_artigo) {

    private var artigoId: Int = -1
    private var artigo: Artigo? = null

    private lateinit var tvTitulo: TextView
    private lateinit var tvAutores: TextView
    private lateinit var tvPeriodico: TextView
    private lateinit var tvAno: TextView
    private lateinit var tvDoi: TextView
    private lateinit var tvLink: TextView
    private lateinit var tvResumo: TextView
    private lateinit var tvPalavrasChave: TextView
    private lateinit var tvPaginas: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var ivLido: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artigoId = arguments?.getInt("artigoId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitulo = view.findViewById(R.id.tvTituloArtigo)
        tvAutores = view.findViewById(R.id.tvAutoresArtigo)
        tvPeriodico = view.findViewById(R.id.tvPeriodicoArtigo)
        tvAno = view.findViewById(R.id.tvAnoArtigo)
        tvDoi = view.findViewById(R.id.tvDoiArtigo)
        tvLink = view.findViewById(R.id.tvLinkArtigo)
        tvResumo = view.findViewById(R.id.tvResumoArtigo)
        tvPalavrasChave = view.findViewById(R.id.tvPalavrasChaveArtigo)
        tvPaginas = view.findViewById(R.id.tvPaginasArtigo)
        ratingBar = view.findViewById(R.id.ratingBarArtigo)
        ivLido = view.findViewById(R.id.ivLidoArtigo)

        carregarArtigo()
    }

    private fun carregarArtigo() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val dao = ArtigoDatabase.getInstance(requireContext()).artigoDao()
            val resultado = dao.buscarPorId(artigoId)

            withContext(Dispatchers.Main) {
                resultado?.let {
                    artigo = it
                    preencherCampos(it)
                }
            }
        }
    }

    private fun preencherCampos(artigo: Artigo) {
        tvTitulo.text = artigo.titulo.ifBlank { getString(R.string.placeholder_titulo_artigo) }
        tvAutores.text = artigo.autores?.ifBlank { getString(R.string.placeholder_autores_nao_informados) } ?: getString(R.string.placeholder_autores_nao_informados)
        tvPeriodico.text = artigo.periodico?.ifBlank { getString(R.string.placeholder_periodico_nao_informado) } ?: getString(R.string.placeholder_periodico_nao_informado)
        tvAno.text = artigo.anoPublicacao?.toString() ?: getString(R.string.placeholder_ano_nao_informado)
        tvDoi.text = artigo.doi?.ifBlank { getString(R.string.placeholder_doi_nao_disponivel) } ?: getString(R.string.placeholder_doi_nao_disponivel)
        tvLink.text = artigo.link?.ifBlank { getString(R.string.placeholder_link_nao_disponivel) } ?: getString(R.string.placeholder_link_nao_disponivel)
        tvResumo.text = artigo.resumo?.ifBlank { getString(R.string.placeholder_resumo_nao_disponivel) } ?: getString(R.string.placeholder_resumo_nao_disponivel)
        tvPalavrasChave.text = artigo.palavrasChave?.ifBlank { getString(R.string.placeholder_palavras_chave_nao_informadas) } ?: getString(R.string.placeholder_palavras_chave_nao_informadas)
        tvPaginas.text = artigo.paginas?.toString() ?: getString(R.string.placeholder_paginas_nao_informadas)
        ratingBar.rating = artigo.nota?.toFloat() ?: 0f

        val corLido = ContextCompat.getColor(requireContext(), R.color.primaryColor)
        val corNaoLido = ContextCompat.getColor(requireContext(), R.color.disabledIcon)
        ivLido.setColorFilter(if (artigo.jaLido) corLido else corNaoLido)
    }

    companion object {
        fun newInstance(artigoId: Int) = DetalhesArtigoFragment().apply {
            arguments = Bundle().apply { putInt("artigoId", artigoId) }
        }
    }
}