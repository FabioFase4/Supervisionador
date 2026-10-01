package com.fabio.eagleeyes.alerta.comportamento

import android.util.Log
import com.fabio.eagleeyes.alerta.Alerta
import com.fabio.eagleeyes.alerta.AlertaRepositorio

import com.fabio.eagleeyes.global.FirebaseConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale
import java.util.UUID

class AlertManager(private val repositorio: AlertaRepositorio) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val ultimoAlertaPorApp = mutableMapOf<String, Long>()
    private var ultimaVezBypass = 0L

    companion object {
        private const val SPAM_DELAY = 60000L
        /*
         * 1 minuto de intervalo = 60 segundos
         * 60 segundos = 60 * 1000 milissegundos
         */
        private const val TAG = "AlertManager"
        /*
        * TAG para Logs
         */
    }

    fun enviarAlertaBloqueio(packageName: String, appLabel: String) {
        val agora = System.currentTimeMillis()
        val ultimaVez = ultimoAlertaPorApp[packageName] ?: 0L

        if (agora - ultimaVez > SPAM_DELAY) {
            // ATUALIZAÇÃO IMEDIATA: Bloqueia chamadas concorrentes antes da Coroutine iniciar
            ultimoAlertaPorApp[packageName] = agora

            val emailRaw = FirebaseConfig.getAuth().currentUser?.email ?: "desconhecido"
            val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val alerta = Alerta(
                id = UUID.randomUUID().toString(),
                aplicativo = appLabel,
                emailFilho = emailRaw,
                horario = horario,
                mensagem = "Tentativa de acesso ao app $appLabel bloqueada por regra de controle.",
                tipo = "BLOQUEIO",
                timestamp = agora
            )

            scope.launch {
                val sucesso = repositorio.salvarAlerta(alerta)
                if (!sucesso) {
                    // Se falhou gravemente, poderíamos resetar o timestamp,
                    // mas geralmente é melhor manter o delay para evitar retry excessivo.
                    Log.e(TAG, "Falha ao salvar alerta para $appLabel")
                } else {
                    Log.d(TAG, "Alerta de bloqueio enviado para $appLabel")
                }
            }
        }
    }

    fun enviarAlertaBypass() {
        val agora = System.currentTimeMillis()

        if (agora - ultimaVezBypass > SPAM_DELAY) {
            ultimaVezBypass = agora // Atualiza imediatamente

            val emailRaw = FirebaseConfig.getAuth().currentUser?.email ?: "desconhecido"
            val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val alerta = Alerta(
                id = UUID.randomUUID().toString(),
                aplicativo = "Configurações do Sistema",
                emailFilho = emailRaw,
                horario = horario,
                mensagem = "O usuário tentou desativar as permissões de monitoramento.",
                tipo = "SEGURANÇA",
                timestamp = agora
            )

            scope.launch {
                repositorio.salvarAlerta(alerta)
                Log.w(TAG, "Alerta de segurança (bypass) enviado!")
            }
        }
    }
}