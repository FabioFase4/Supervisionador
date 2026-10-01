package com.fabio.eagleeyes.alerta.comportamento

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

import com.fabio.eagleeyes.alerta.Alerta
import com.fabio.eagleeyes.alerta.AlertaDAO
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.global.NotificationHelper

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class AlertListenerService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private val alertaDao = AlertaDAO()
    private val TAG = "EagleEyes_Service"

    private val startTime = System.currentTimeMillis() - (60 * 60 * 1000)
    private val alertasProcessados = mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        iniciarForeground()
        iniciarEscutaAlertas()
    }

    private fun iniciarForeground() {
        val channelId = "eagleeyes_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Serviço de Monitoramento", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("EagleEyes Ativo")
            .setContentText("Monitorando alertas de segurança...")
            .setSmallIcon(R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1001, notification)
        }
    }

    private fun iniciarEscutaAlertas() {
        val user = FirebaseConfig.getAuth().currentUser ?: return
        val emailPai = user.email ?: return
        val refPai = alertaDao.getAlertasRef(emailPai)

        refPai.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.ref.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(alertSnap: DataSnapshot, prevName: String?) {
                        val alerta = alertSnap.getValue(Alerta::class.java)
                        val id = alertSnap.key ?: ""

                        if (alerta != null && !alertasProcessados.contains(id)) {
                            if (alerta.timestamp > startTime) {
                                alertasProcessados.add(id)
                                notificationHelper.showNotification(
                                    "Alerta: ${alerta.aplicativo}",
                                    alerta.mensagem
                                )
                            }
                        }
                    }
                    override fun onChildChanged(s: DataSnapshot, p: String?) {}
                    override fun onChildRemoved(s: DataSnapshot) {}
                    override fun onChildMoved(s: DataSnapshot, p: String?) {}
                    override fun onCancelled(e: DatabaseError) {}
                })
            }
            override fun onChildChanged(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onCancelled(e: DatabaseError) {}
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
