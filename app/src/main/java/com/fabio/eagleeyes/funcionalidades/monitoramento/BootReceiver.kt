package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

import com.fabio.eagleeyes.global.FirebaseConfig

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Só inicia o serviço se houver um usuário logado (filho)
            val user = FirebaseConfig.getAuth().currentUser
            if (user != null) {
                val serviceIntent = Intent(context, MonitoramentoService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(serviceIntent)
                else
                    context.startService(serviceIntent)
            }
        }
    }
}