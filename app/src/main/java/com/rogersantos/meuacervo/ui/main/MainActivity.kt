package com.rogersantos.meuacervo.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.rogersantos.meuacervo.MeuAcervoApplication
import com.rogersantos.meuacervo.R
import com.rogersantos.meuacervo.data.database.AppDatabase
import com.rogersantos.meuacervo.ui.controle.MeuControleActivity
import com.rogersantos.meuacervo.ui.estante.EstanteActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var txtSubtitulo: TextView
    private lateinit var txtTotalLivros: TextView
    private lateinit var txtEmprestados: TextView
    private lateinit var txtLidosAno: TextView
    private lateinit var txtTotalArtigos: TextView
    private lateinit var btnAbrirEstante: MaterialButton
    private lateinit var btnMeuControle: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MeuAcervo)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        txtSubtitulo = findViewById(R.id.txtSubtitulo)
        txtTotalLivros = findViewById(R.id.txtTotalLivros)
        txtEmprestados = findViewById(R.id.txtEmprestados)
        txtLidosAno = findViewById(R.id.txtLidosAno)
        txtTotalArtigos = findViewById(R.id.txtTotalArtigos)
        btnAbrirEstante = findViewById(R.id.btnAbrirEstante)
        btnMeuControle = findViewById(R.id.btnMeuControle)

        // Botões desabilitados até a migração do banco confirmar
        btnAbrirEstante.isEnabled = false
        btnMeuControle.isEnabled = false

        lifecycleScope.launch {
            (application as MeuAcervoApplication).migracaoConcluida.await()
            btnAbrirEstante.isEnabled = true
            btnMeuControle.isEnabled = true
            carregarEstatisticas()
        }

        btnAbrirEstante.setOnClickListener {
            startActivity(Intent(this, EstanteActivity::class.java))
        }

        btnMeuControle.setOnClickListener {
            startActivity(Intent(this, MeuControleActivity::class.java))
        }

        criarCanalNotificacao()
        solicitarPermissaoNotificacao()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza os contadores ao voltar para a tela principal
        lifecycleScope.launch { carregarEstatisticas() }
    }

    private suspend fun carregarEstatisticas() {
        val db = AppDatabase.getInstance(applicationContext)
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR).toString()

        val totalLivros: Int
        val emprestados: Int
        val lidosAno: Int
        val totalArtigos: Int

        withContext(Dispatchers.IO) {
            totalLivros = db.livroDao().listarTodos().size
            emprestados = db.emprestimoDao().listarTodos()
                .count { !it.devolvido }
            lidosAno = db.livroDao().listarTodos()
                .count { it.jaLido }
            totalArtigos = db.artigoDao().listarTodos().size
        }

        val subtitulo = "$totalLivros livros · $totalArtigos artigos"
        txtSubtitulo.text = subtitulo
        txtTotalLivros.text = totalLivros.toString()
        txtEmprestados.text = emprestados.toString()
        txtLidosAno.text = lidosAno.toString()
        txtTotalArtigos.text = totalArtigos.toString()
    }

    private fun criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "lembrete_channel",
                getString(R.string.channel_lembrete_nome),
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_lembrete_descricao)
            }
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.msg_permissao_concedida), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.msg_permissao_negada), Toast.LENGTH_SHORT).show()
            }
        }
    }
}