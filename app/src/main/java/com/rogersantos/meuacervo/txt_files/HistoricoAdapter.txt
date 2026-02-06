package com.rogersantos.meuacervo.ui.controle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.LivroDatabase
import com.rogersantos.meuacervo.data.model.Emprestimo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoricoAdapter :
    ListAdapter<Emprestimo, HistoricoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPessoa: TextView = itemView.findViewById(R.id.tvPessoaHistorico)
        private val tvTelefone: TextView = itemView.findViewById(R.id.tvTelefoneHistorico)
        private val tvDataEmprestimo: TextView = itemView.findViewById(R.id.tvDataEmprestimoHistorico)
        private val tvDataPrevista: TextView = itemView.findViewById(R.id.tvDataPrevistaHistorico)
        private val tvDataDevolucao: TextView = itemView.findViewById(R.id.tvDataDevolucaoHistorico)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusHistorico)
        private val tvLivro: TextView? = itemView.findViewById(R.id.tvLivroHistorico)
        private val ivCapa: ImageView? = itemView.findViewById(R.id.ivCapaHistorico)

        fun bind(e: Emprestimo) {
            tvPessoa.text = e.pessoa
            tvTelefone.text = e.telefone ?: "-"
            tvDataEmprestimo.text = itemView.context.getString(R.string.historico_emprestado_em, e.dataEmprestimo)
            tvDataPrevista.text = if (e.dataPrevistaDevolucao != null) {
                itemView.context.getString(R.string.historico_previsao, e.dataPrevistaDevolucao)
            } else {
                itemView.context.getString(R.string.historico_sem_prazo)
            }
            tvDataDevolucao.text = if (e.devolvido) itemView.context.getString(R.string.historico_devolvido_em, e.dataDevolucao) else itemView.context.getText(R.string.historico_ainda_nao_devolvido)
            tvStatus.text = if (e.devolvido) itemView.context.getString(R.string.historico_devolvido) else itemView.context.getString(R.string.historico_ativo)

            // Se quiser mostrar título/capa do livro vinculado
            if (tvLivro != null && ivCapa != null) {
                val dao = LivroDatabase.getInstance(itemView.context).livroDao()
                CoroutineScope(Dispatchers.IO).launch {
                    val livro = dao.buscarPorId(e.livroId)
                    withContext(Dispatchers.Main) {
                        if (livro != null) {
                            tvLivro.text = livro.titulo
                            val capa = livro.capaPath ?: livro.urlCapa
                            if (!capa.isNullOrBlank()) {
                                // Se estiver usando Glide ou similar
                                // Glide.with(itemView.context)
                                //     .load(capa)
                                //     .placeholder(R.drawable.ic_capa_placeholder)
                                //     .into(ivCapa)
                            } else {
                                ivCapa.setImageResource(R.drawable.ic_capa_placeholder)
                            }
                        }
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Emprestimo>() {
        override fun areItemsTheSame(o: Emprestimo, n: Emprestimo) = o.id == n.id
        override fun areContentsTheSame(o: Emprestimo, n: Emprestimo) = o == n
    }
}