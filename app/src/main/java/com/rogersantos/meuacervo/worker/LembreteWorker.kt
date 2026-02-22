package com.rogersantos.meuacervo.worker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rogersantos.meuacervo.R

class LembreteWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        val titulo = inputData.getString("titulo") ?: applicationContext.getString(R.string.placeholder_livro)
        val pessoa = inputData.getString("pessoa") ?: applicationContext.getString(R.string.placeholder_contato)

        val builder = NotificationCompat.Builder(applicationContext, "lembrete_channel")
            .setSmallIcon(R.drawable.ic_add_book) // ícone em drawable
            .setContentTitle(applicationContext.getString(R.string.notification_devolucao_proxima))
            .setContentText(applicationContext.getString(R.string.notification_texto_devolucao, titulo, pessoa))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(applicationContext)

        // ✅ Verifica se a permissão foi concedida antes de notificar
        val granted = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            manager.notify(System.currentTimeMillis().toInt(), builder.build())
        } else {
            Log.w("LembreteWorker", "Permissão POST_NOTIFICATIONS não concedida")
        }

        return Result.success()
    }
}