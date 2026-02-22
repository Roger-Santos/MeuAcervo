package com.rogersantos.meuacervo.ui.opcoes

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rogersantos.meuacervo.R

class OpcoesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSalvar: Button
    private lateinit var adapter: CategoriaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opcoes)

        recyclerView = findViewById(R.id.recyclerCategorias)
        btnSalvar = findViewById(R.id.btnSalvar)

        val prefs = getSharedPreferences("config", MODE_PRIVATE)
        val salvas = prefs.getString("categoriasOrdenadas", null)?.split("|") ?: listOf(
            getString(R.string.categoria_titulo), getString(R.string.categoria_subtitulo), getString(R.string.categoria_autor), getString(R.string.categoria_local), getString(R.string.categoria_ano), getString(R.string.categoria_editora)
        )
        val selecionadas = prefs.getStringSet("categoriasSelecionadas", salvas.toSet())!!.toMutableSet()

        adapter = CategoriaAdapter(salvas.toMutableList(), selecionadas)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                adapter.moveItem(vh.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        touchHelper.attachToRecyclerView(recyclerView)

        btnSalvar.setOnClickListener {
            val prefs = getSharedPreferences("config", MODE_PRIVATE)
            prefs.edit()
                .putString("categoriasOrdenadas", adapter.categorias.joinToString("|"))
                .putStringSet("categoriasSelecionadas", adapter.selecionadas)
                .apply()

            Toast.makeText(this, getString(R.string.msg_categoria_salvas), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}