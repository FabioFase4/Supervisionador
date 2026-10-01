package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fabio.eagleeyes.R
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.repositorio.UsoRepositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonitoramentoService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var usoRepositorio: UsoRepositorio
    private val TAG = "MonitoramentoService"

    override fun onCreate() {
        super.onCreate()
        usoRepositorio = UsoRepositorio(this)
        iniciarServico()
        iniciarLoopMonitoramento()
    }

    private fun iniciarServico() {
        val channelId = "monitoramento_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Monitoramento EagleEyes",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("EagleEyes em execução")
            .setContentText("Protegendo este dispositivo...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    private fun iniciarLoopMonitoramento() {
        serviceScope.launch {
            while (isActive) {
                try {
                    sincronizarUsoComFirebase()
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao sincronizar uso: ${e.message}")
                }
                delay(60 * 1000) // Sincroniza a cada 1 minuto para testes, considere aumentar para produção
            }
        }
    }

    private fun sincronizarUsoComFirebase() {
        val user = FirebaseConfig.getAuth().currentUser ?: return
        val uid = user.uid

        val dadosUso = usoRepositorio.buscarDadosUso()
        val dataAtual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // CAMINHO ALTERADO: historicoUso/{uid}/{data}
        val ref = FirebaseConfig.getHistoricoUsoRef()
            .child(uid)
            .child(dataAtual)

        val mapaUso = mutableMapOf<String, Any>()
        for (par in dadosUso) {
            val packageLimpo = par.first.replace(".", "_")
            val tempoEmMinutos = par.second / 60000
            if (tempoEmMinutos > 0) {
                mapaUso[packageLimpo] = tempoEmMinutos
            }
        }

        if (mapaUso.isNotEmpty()) {
            ref.updateChildren(mapaUso)
                .addOnSuccessListener { Log.d(TAG, "Uso sincronizado em historicoUso para o UID: $uid") }
                .addOnFailureListener { e -> Log.e(TAG, "Falha ao sincronizar: ${e.message}") }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
