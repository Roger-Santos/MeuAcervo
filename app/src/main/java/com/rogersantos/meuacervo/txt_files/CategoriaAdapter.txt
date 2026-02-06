package com.rogersantos.meuacervo.ui.opcoes

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter(
    val categorias: MutableList<String>,
    val selecionadas: MutableSet<String>
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    inner class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val checkBox = CheckBox(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }

        layout.addView(checkBox)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = categorias[position]
        val checkBox = holder.layout.getChildAt(0) as CheckBox
        checkBox.text = categoria
        checkBox.isChecked = selecionadas.contains(categoria)

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selecionadas.add(categoria) else selecionadas.remove(categoria)
        }
    }

    override fun getItemCount(): Int = categorias.size

    fun moveItem(from: Int, to: Int) {
        val item = categorias.removeAt(from)
        categorias.add(to, item)
        notifyItemMoved(from, to)
    }
}