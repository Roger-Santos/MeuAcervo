package com.rogersantos.meuacervo.ui.estante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.model.Livro

class LivroAdapter(
    private val onClick: (Livro) -> Unit
) : ListAdapter<Livro, LivroAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro_estante, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val livro = getItem(position)
        holder.bind(livro, onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgCapa: ImageView = itemView.findViewById(R.id.ivCapaItem)
        private val txtTitulo: TextView = itemView.findViewById(R.id.tvTituloItem)
        private val txtAutor: TextView = itemView.findViewById(R.id.tvAutorItem)
        private val txtCategoria: TextView? = itemView.findViewById(R.id.tvCategoriaItem)
        private val txtPaginas: TextView? = itemView.findViewById(R.id.tvPaginasItem)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarItem)
        private val imgLido: ImageView = itemView.findViewById(R.id.ivLido)

        fun bind(livro: Livro, onClick: (Livro) -> Unit) {
            val context = itemView.context

            // Título (sempre 2 linhas, ellipsize já definido no XML)
            txtTitulo.text = livro.titulo.ifBlank { itemView.context.getString(R.string.placeholder_titulo_livro) }

            // Autor
            txtAutor.text = livro.autores?.ifBlank { itemView.context.getString(R.string.placeholder_autores_livro) } ?: itemView.context.getString(R.string.placeholder_autores_livro)

            // Categoria
            txtCategoria?.text = livro.categoria?.ifBlank { itemView.context.getString(R.string.placeholder_categoria_livro) } ?: itemView.context.getString(R.string.placeholder_categoria_livro)

            // Páginas
            txtPaginas?.text = livro.paginas?.takeIf { it > 0 }?.let { itemView.context.getString(R.string.label_paginas_format, it) } ?: itemView.context.getString(R.string.placeholder_paginas_livro)

            // Nota
            ratingBar.rating = livro.nota?.toFloat() ?: 0f

            // Ícone de "lido" — visível só quando o livro foi marcado como lido
            imgLido.visibility = if (livro.jaLido) View.VISIBLE else View.GONE

            // Capa
            Glide.with(context).clear(imgCapa)

            val requestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_capa_placeholder)
                .error(R.drawable.ic_capa_placeholder)

            when {
                !livro.capaPath.isNullOrBlank() -> {
                    Glide.with(context)
                        .load(livro.capaPath)
                        .apply(requestOptions)
                        .into(imgCapa)
                }
                !livro.urlCapa.isNullOrBlank() -> {
                    val url = if (livro.urlCapa.startsWith("//")) "https:${livro.urlCapa}" else livro.urlCapa
                    Glide.with(context)
                        .load(url)
                        .apply(requestOptions)
                        .into(imgCapa)
                }
                else -> {
                    imgCapa.setImageResource(R.drawable.ic_capa_placeholder)
                }
            }

            // Clique no card
            itemView.setOnClickListener { onClick(livro) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Livro>() {
        override fun areItemsTheSame(oldItem: Livro, newItem: Livro): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Livro, newItem: Livro): Boolean = oldItem == newItem
    }
}