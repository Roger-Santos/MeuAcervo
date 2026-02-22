package com.rogersantos.meuacervo.ui.controle

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.content.ContextCompat
import com.rogersantos.meuacervo.R

class MeuControleActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meu_controle)

        // Toolbar com título branco e botão de voltar
        val toolbar: Toolbar = findViewById(R.id.toolbarControle)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.title_meu_controle)
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))

        viewPager = findViewById(R.id.viewPagerControle)
        tabLayout = findViewById(R.id.tabLayoutControle)

        val adapter = ControlePagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = if (pos == 0)  getString(R.string.tab_livros) else  getString(R.string.tab_historico)
        }.attach()

        criarCanalNotificacao()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun criarCanalNotificacao() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "lembrete_channel",
                getString(R.string.channel_lembrete_nome),
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}