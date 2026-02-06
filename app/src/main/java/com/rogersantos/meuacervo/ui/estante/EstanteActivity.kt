package com.rogersantos.meuacervo.ui.estante

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.rogersantos.meuacervo.ui.cadastro.CadastroArtigoActivity
import com.rogersantos.meuacervo.ui.cadastro.CadastroLivroActivity
import com.rogersantos.meuacervo.R

// Imports do AdMob
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdView

class EstanteActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton

    // Variáveis para anúncio
    private var interstitialAd: InterstitialAd? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var adView: AdView   // Banner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estante)

        // Inicializa SharedPreferences
        prefs = getSharedPreferences("meuacervo_prefs", MODE_PRIVATE)

        // Toolbar moderna
        toolbar = findViewById(R.id.toolbarEstante)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.titulo_estante)
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setNavigationOnClickListener { finish() }

        // Tabs + ViewPager
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        fabAdd = findViewById(R.id.fabAdd)

        viewPager.adapter = EstantePagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = if (pos == 0) getString(R.string.aba_livros)
            else getString(R.string.aba_artigos)
        }.attach()

        atualizarFabParaAba(0)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                atualizarFabParaAba(position)
            }
        })

        fabAdd.setOnClickListener {
            when (viewPager.currentItem) {
                0 -> startActivity(Intent(this, CadastroLivroActivity::class.java))
                1 -> startActivity(Intent(this, CadastroArtigoActivity::class.java))
            }
        }

        // Incrementa contador persistido
        val contador = incrementarContador()
        Log.d("AD", "Resultado: " + (contador % 10))
        // Exibe anúncio intersticial a cada 10 acessos
        if (contador % 10 == 0) {
            carregarInterstitial()
        }

        // Banner fixo no rodapé
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun atualizarFabParaAba(position: Int) {
        when (position) {
            0 -> {
                fabAdd.setImageResource(R.drawable.ic_add_book)
                fabAdd.contentDescription = getString(R.string.btn_adicionar_livro)
            }
            1 -> {
                fabAdd.setImageResource(R.drawable.ic_add_book)
                fabAdd.contentDescription = getString(R.string.btn_adicionar_artigo)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    // Função para incrementar contador usando SharedPreferences
    private fun incrementarContador(): Int {
        val atual = prefs.getInt("contador_estante", 0) + 1
        prefs.edit().putInt("contador_estante", atual).apply()
        return atual
    }

    // Funções para carregar e mostrar intersticial
    private fun carregarInterstitial() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-6771346576183891/2959750886", // ✅ ID real do AdMob
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    mostrarInterstitial()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun mostrarInterstitial() {
        interstitialAd?.show(this)
    }

    // Ciclo de vida do Banner
    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}