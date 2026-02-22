package com.rogersantos.meuacervo.ui.detalhes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesLivroActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var livroId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_livro)

        val toolbar: Toolbar = findViewById(R.id.toolbarDetalhes)
        setSupportActionBar(toolbar)

        // seta de navegação sempre branca
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))

        // título sempre branco
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        // Força os ícones do menu (incluindo os três pontos) a ficarem brancos
        toolbar.overflowIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))

        livroId = intent.getIntExtra("livroId", -1)

        // Atualiza título dinamicamente com nome do livro
        if (livroId != -1) {
            MainScope().launch {
                val dao = AppDatabase.getInstance(applicationContext).livroDao()
                val livro = withContext(Dispatchers.IO) { dao.buscarPorId(livroId) }
                livro?.let { supportActionBar?.title = it.titulo }
            }
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = DetalhesPagerAdapter(this, livroId)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.tab_detalhes) else getString(R.string.tab_editar)
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detalhes_livro, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_excluir_livro -> {
                confirmarExclusaoLivro()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmarExclusaoLivro() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.title_confirmar_exclusao))
            .setMessage(getString(R.string.msg_confirmar_exclusao_livro))
            .setPositiveButton(getString(R.string.btn_excluir)) { _, _ ->
                if (livroId != -1) {
                    MainScope().launch {
                        val dao = AppDatabase.getInstance(applicationContext).livroDao()
                        withContext(Dispatchers.IO) {
                            val livro = dao.buscarPorId(livroId)
                            livro?.let { dao.deletar(it) }
                        }
                        Toast.makeText(this@DetalhesLivroActivity, getString(R.string.msg_livro_excluido), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            .setNegativeButton(getString(R.string.btn_cancelar), null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza título se o livro foi editado
        if (livroId != -1) {
            MainScope().launch {
                val dao = AppDatabase.getInstance(applicationContext).livroDao()
                val livro = withContext(Dispatchers.IO) { dao.buscarPorId(livroId) }
                livro?.let { supportActionBar?.title = it.titulo }
            }
        }
    }
}