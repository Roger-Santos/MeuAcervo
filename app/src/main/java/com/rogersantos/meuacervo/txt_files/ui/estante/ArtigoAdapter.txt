package com.rogersantos.meuacervo.ui.estante

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.model.Artigo

class ArtigoAdapter(
    private val onClick: (Artigo) -> Unit
) : ListAdapter<Artigo, ArtigoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_artigo_estante, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artigo = getItem(position)
        holder.bind(artigo, onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitulo: TextView = itemView.findViewById(R.id.tvTituloArtigoItem)
        private val txtAutores: TextView = itemView.findViewById(R.id.tvAutoresArtigoItem)
        private val txtPeriodico: TextView = itemView.findViewById(R.id.tvPeriodicoArtigoItem)
        private val txtAno: TextView = itemView.findViewById(R.id.tvAnoArtigoItem)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarArtigoItem)
        private val imgLido: ImageView = itemView.findViewById(R.id.ivLidoArtigo)

        fun bind(artigo: Artigo, onClick: (Artigo) -> Unit) {
            txtTitulo.text = artigo.titulo
            txtAutores.text = artigo.autores ?: ""
            txtPeriodico.text = artigo.periodico ?: ""
            txtAno.text = artigo.anoPublicacao?.toString() ?: ""

            ratingBar.rating = artigo.nota?.toFloat() ?: 0f

            // Ícone de "lido"
            val context = itemView.context
            val corLido = ContextCompat.getColor(context, R.color.primaryColor)
            val corNaoLido = ContextCompat.getColor(context, R.color.disabledIcon)
            imgLido.setColorFilter(if (artigo.jaLido) corLido else corNaoLido)

            itemView.setOnClickListener { onClick(artigo) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Artigo>() {
        override fun areItemsTheSame(oldItem: Artigo, newItem: Artigo): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Artigo, newItem: Artigo): Boolean = oldItem == newItem
    }
}