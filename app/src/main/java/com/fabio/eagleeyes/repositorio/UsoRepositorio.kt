package com.fabio.eagleeyes.repositorio

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

class UsoRepositorio(private val context: Context) {

    fun buscarDadosUso(): List<Pair<String, Long>> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendario = Calendar.getInstance()
        val fimTempo = calendario.timeInMillis

        calendario.add(Calendar.DAY_OF_YEAR, -1)
        val inicioTempo = calendario.timeInMillis

        val status = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            inicioTempo,
            fimTempo
        )
        val result = mutableListOf<Pair<String, Long>>()

        if (status != null) {
            for (app in status) {
                if (app.totalTimeInForeground > 0) {
                    result.add(Pair(app.packageName, app.totalTimeInForeground))
                }
            }
        }
        return result.sortedByDescending { it.second }
    }

    fun formatarTempo(ms: Long): String {
        val segundos = ms / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        return "${horas}h ${minutos % 60}m"
    }
}