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
import com.rogersantos.meuacervo.data.database.ArtigoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalhesArtigoActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var artigoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_artigo)

        val toolbar: Toolbar = findViewById(R.id.toolbarDetalhesArtigo)
        setSupportActionBar(toolbar)

        // Navegação e título sempre branco
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        artigoId = intent.getIntExtra("artigoId", -1)

        // Atualiza título da Toolbar com o nome do artigo
        if (artigoId != -1) {
            MainScope().launch {
                val dao = ArtigoDatabase.getInstance(applicationContext).artigoDao()
                val artigo = withContext(Dispatchers.IO) { dao.buscarPorId(artigoId) }
                artigo?.let { supportActionBar?.title = it.titulo }
            }
        }

        viewPager = findViewById(R.id.viewPagerArtigo)
        tabLayout = findViewById(R.id.tabLayoutArtigo)

        // Estilo das abas via código (selecionada azul, não selecionada cinza)
        val primary = ContextCompat.getColor(this, R.color.primaryColor)
        val secondaryText = ContextCompat.getColor(this, R.color.textSecondary)
        tabLayout.setSelectedTabIndicatorColor(primary)
        tabLayout.setTabTextColors(secondaryText, primary)
        tabLayout.setSelectedTabIndicatorHeight(resources.getDimensionPixelSize(R.dimen.tab_indicator_height))

        val adapter = DetalhesArtigoPagerAdapter(this, artigoId)
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
        // Atualiza título se o artigo foi editado
        if (artigoId != -1) {
            MainScope().launch {
                val dao = ArtigoDatabase.getInstance(applicationContext).artigoDao()
                val artigo = withContext(Dispatchers.IO) { dao.buscarPorId(artigoId) }
                artigo?.let { supportActionBar?.title = it.titulo }
            }
        }
    }
}