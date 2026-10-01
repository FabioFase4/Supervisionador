package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

import com.fabio.eagleeyes.alerta.AlertaRepositorio
import com.fabio.eagleeyes.alerta.comportamento.AlertManager
import com.fabio.eagleeyes.funcionalidades.monitoramento.activity.BloqueioActivity
import com.fabio.eagleeyes.funcionalidades.monitoramento.activity.TentativaDesinstalacaoActivity
import com.fabio.eagleeyes.global.FirebaseConfig

class BloqueioAppAccessibilityService : AccessibilityService() {

    private var syncManager: FirebaseSyncManager? = null
    private var ruleEvaluator: UsageRuleEvaluator? = null
    private var alertManager: AlertManager? = null
    private val TAG = "EagleEyesMonitor"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Serviço de Acessibilidade Conectado")
        inicializarComponentes()
    }

    private fun inicializarComponentes() {
        val user = FirebaseConfig.getAuth().currentUser
        val emailFilho = user?.email

        if (emailFilho == null) {
            Log.e(TAG, "Monitoramento não iniciado: Usuário não logado no Firebase.")
            return
        }

        if (syncManager == null) {
            syncManager = FirebaseSyncManager(emailFilho)
            syncManager?.startSync()
        }
        
        if (ruleEvaluator == null) {
            ruleEvaluator = UsageRuleEvaluator(this)
        }
        
        if (alertManager == null) {
            alertManager = AlertManager(AlertaRepositorio())
        }

        Log.d(TAG, "Componentes inicializados para $emailFilho")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (syncManager == null) {
            inicializarComponentes()
        }

        val sync = syncManager ?: return
        val evaluator = ruleEvaluator ?: return
        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName || packageName.contains("fabio.eagleyes")) return

        // 1. Proteção contra Burla (Desinstalação, Limpar Dados, Forçar Parada)
        if (verificarTentativaBurla(event, sync)) {
            Log.w(TAG, "Interação proibida detectada em: $packageName")
            executarBloqueio(packageName, isBurla = true)
            return
        }

        // 2. Filtro de Eventos de Mudança de Estado (Abertura de Apps Bloqueados)
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (evaluator.shouldBlock(packageName, sync.regrasAtivas)) {
                Log.w(TAG, "App bloqueado por regra de uso: $packageName")
                executarBloqueio(packageName, isBurla = false)
            }
        }
    }

    private fun verificarTentativaBurla(event: AccessibilityEvent, sync: FirebaseSyncManager): Boolean {
        val packageName = event.packageName?.toString() ?: ""
        
        val isConfigApp = packageName.contains("settings", ignoreCase = true) ||
                          packageName.contains("packageinstaller", ignoreCase = true) ||
                          packageName.contains("vending", ignoreCase = true) || 
                          sync.nomesApp.any { packageName.contains(it, ignoreCase = true) }

        if (!isConfigApp) return false

        val source = event.source ?: rootInActiveWindow ?: return false
        
        val termosPadrao = listOf("desinstalar", "uninstall", "limpar dados", "clear data", "forçar parada", "force stop", "desativar")
        val termosParaVerificar = if (sync.termosBloqueio.isNotEmpty()) sync.termosBloqueio else termosPadrao

        return scanForForbiddenTerms(source, termosParaVerificar)
    }

    private fun scanForForbiddenTerms(node: AccessibilityNodeInfo, termos: List<String>): Boolean {
        for (termo in termos) {
            if (termo.length < 3) continue 
            
            val foundNodes = node.findAccessibilityNodeInfosByText(termo)
            if (!foundNodes.isNullOrEmpty()) {
                for (foundNode in foundNodes) {
                    val text = foundNode.text?.toString()?.lowercase() ?: ""
                    val content = foundNode.contentDescription?.toString()?.lowercase() ?: ""
                    val termoLower = termo.lowercase()
                    
                    if (text.contains(termoLower) || content.contains(termoLower)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun executarBloqueio(packageName: String, isBurla: Boolean) {
        val appName = getAppName(packageName)

        // 1. Volta para a Home do dispositivo imediatamente (minimiza o que o filho estava fazendo)
        performGlobalAction(GLOBAL_ACTION_HOME)

        // 2. Envia o alerta correto para o Firebase (Visão do Pai)
        if (isBurla) {
            alertManager?.enviarAlertaBypass()
        } else {
            alertManager?.enviarAlertaBloqueio(packageName, appName)
        }

        // 3. Direciona o filho para a tela de aviso específica correspondente
        val destinoActivity = if (isBurla) {
            TentativaDesinstalacaoActivity::class.java
        } else {
            BloqueioActivity::class.java
        }

        val intent = Intent(this, destinoActivity).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("APP_BLOQUEADO", appName)
        }
        startActivity(intent)
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
    }

    override fun onInterrupt() {}
}
