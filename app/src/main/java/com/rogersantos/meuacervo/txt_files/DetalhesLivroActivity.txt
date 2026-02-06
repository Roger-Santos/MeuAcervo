package com.rogersantos.meuacervo.ui.detalhes

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.LivroDatabase
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

        livroId = intent.getIntExtra("livroId", -1)

        // Atualiza título dinamicamente com nome do livro
        if (livroId != -1) {
            MainScope().launch {
                val dao = LivroDatabase.getInstance(applicationContext).livroDao()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Atualiza título se o livro foi editado
        if (livroId != -1) {
            MainScope().launch {
                val dao = LivroDatabase.getInstance(applicationContext).livroDao()
                val livro = withContext(Dispatchers.IO) { dao.buscarPorId(livroId) }
                livro?.let { supportActionBar?.title = it.titulo }
            }
        }
    }
}