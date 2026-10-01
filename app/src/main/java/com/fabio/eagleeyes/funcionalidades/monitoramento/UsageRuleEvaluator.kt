package com.fabio.eagleeyes.funcionalidades.monitoramento

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.fabio.eagleeyes.funcionalidades.regra.Regra
import java.util.Calendar

class UsageRuleEvaluator(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val TAG = "UsageRuleEvaluator"

    fun shouldBlock(packageName: String, regras: List<Regra>): Boolean {
        // Apps que nunca devem ser bloqueados (Sistema e o próprio Supervisionador)
        if (isAppSistema(packageName)) return false

        val labelApp = getAppName(packageName)

        for (regra in regras) {
            if (!regra.ativa) continue

            // Verifica se a regra se aplica a este app ou se é uma regra "Geral"
            val appCorresponde = regra.nomeApp.equals(labelApp, ignoreCase = true) ||
                                 regra.nomeApp.equals(packageName, ignoreCase = true) ||
                                 regra.nomeApp.equals("Geral", ignoreCase = true)

            if (appCorresponde) {
                val blocked = when (regra.tipo) {
                    "BLOQUEIO" -> true
                    
                    "HORARIO" -> {
                        estaForaDoHorarioPermitido(regra.valor)
                    }

                    "LIMITE_TEMPO" -> {
                        val limiteMinutos = regra.valor.toLongOrNull() ?: Long.MAX_VALUE
                        val tempoGastoMs = getTempoUsoHoje(packageName)
                        val tempoGastoMinutos = tempoGastoMs / 60000
                        tempoGastoMinutos >= limiteMinutos
                    }
                    else -> false
                }

                if (blocked) {
                    Log.w(TAG, "Regra acionada: App=$packageName Tipo=${regra.tipo} Valor=${regra.valor}")
                    return true
                }
            }
        }
        return false
    }

    private fun isAppSistema(packageName: String): Boolean {
        return packageName.contains("launcher", ignoreCase = true) || 
               packageName == "android" || 
               packageName == "com.android.systemui" ||
               packageName == "com.google.android.googlequicksearchbox" ||
               packageName == context.packageName ||
               packageName.contains("fabio.eagleyes", ignoreCase = true)
    }

    private fun estaForaDoHorarioPermitido(intervalo: String): Boolean {
        return try {
            val partes = intervalo.split("-")
            if (partes.size < 2) return false
            
            val agora = Calendar.getInstance()
            val minAgora = agora.get(Calendar.HOUR_OF_DAY) * 60 + agora.get(Calendar.MINUTE)
            
            val minInicio = converterParaMinutos(partes[0])
            val minFim = converterParaMinutos(partes[1])

            if (minInicio < minFim) {
                // Caso comum: 08:00 - 20:00 (Permitido neste intervalo)
                // Bloqueia se for ANTES do início ou APÓS o fim
                minAgora < minInicio || minAgora > minFim
            } else {
                // Caso atravessa a meia-noite: 22:00 - 06:00 (Permitido neste intervalo)
                // Bloqueia se estiver no MEIO do dia (ex: entre 06:01 e 21:59)
                minAgora in minFim until minInicio
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao processar horário: $intervalo", e)
            false
        }
    }

    private fun converterParaMinutos(hora: String): Int {
        val t = hora.trim().split(":")
        val h = t[0].toInt()
        val m = if (t.size > 1) t[1].toInt() else 0
        return h * 60 + m
    }

    private fun getTempoUsoHoje(packageName: String): Long {
        return try {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, 
                cal.timeInMillis, 
                System.currentTimeMillis()
            )
            
            stats?.filter { it.packageName == packageName }?.sumOf { it.totalTimeInForeground } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter tempo de uso para $packageName", e)
            0
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
    }
}
