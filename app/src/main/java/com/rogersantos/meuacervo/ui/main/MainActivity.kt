package com.rogersantos.meuacervo.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.rogersantos.meuacervo.MeuAcervoApplication
import com.rogersantos.meuacervo.ui.estante.EstanteActivity
import com.rogersantos.meuacervo.ui.controle.MeuControleActivity
import com.rogersantos.meuacervo.R
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MeuAcervo)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        // Botões com navegação
        val btnAbrirEstante = findViewById<MaterialButton>(R.id.btnAbrirEstante)
        val btnMeuControle = findViewById<MaterialButton>(R.id.btnMeuControle)

        // Desabilitados até confirmarmos que a migração do banco antigo
        // (se houver) já terminou — evita abrir a estante "vazia" por engano
        // numa eventual primeira execução após a atualização do app.
        btnAbrirEstante.isEnabled = false
        btnMeuControle.isEnabled = false

        lifecycleScope.launch {
            (application as MeuAcervoApplication).migracaoConcluida.await()
            btnAbrirEstante.isEnabled = true
            btnMeuControle.isEnabled = true
        }

        btnAbrirEstante.setOnClickListener {
            startActivity(Intent(this, EstanteActivity::class.java))
        }

        btnMeuControle.setOnClickListener {
            startActivity(Intent(this, MeuControleActivity::class.java))
        }

        // Permissões e notificações
        criarCanalNotificacao()
        solicitarPermissaoNotificacao()
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
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
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