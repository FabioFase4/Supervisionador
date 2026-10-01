package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.view.accessibility.AccessibilityNodeInfo

class SecurityPolicyManager {

    // Lista de fallback caso o Firebase esteja offline
    private val namesToBlockBase = listOf("EagleEyes", "Segurança - Controle Parental")

    // Lista de segurança (Fallback) para os botões de perigo
    private val botoesPerigoBase = listOf(
        "Desinstalar", "Forçar parada", "Desativar", "Desabilitar",
        "Uninstall", "Force stop", "Deactivate", "Limpar dados", "Clear data"
    )

    fun isAttemptingToBypass(
        rootNode: AccessibilityNodeInfo?,
        termosBloqueio: List<String>,
        nomesApp: List<String>
    ): Boolean {
        if (rootNode == null) return false

        val listaNomes = if (nomesApp.isNotEmpty()) nomesApp else namesToBlockBase
        var temNomeApp = false

        for (name in listaNomes) {
            if (rootNode.findAccessibilityNodeInfosByText(name).isNotEmpty()) {
                temNomeApp = true
                break
            }
        }

        // Se o nome do app não está na tela, não é uma tentativa direta de mexer nele
        if (!temNomeApp) return false

        // Usa a lista do Firebase ou a Base de fallback
        val listaTermos = if (termosBloqueio.isNotEmpty()) termosBloqueio else botoesPerigoBase

        for (botao in listaTermos) {
            if (rootNode.findAccessibilityNodeInfosByText(botao).isNotEmpty())
                return true
        }

        return false
    }
}