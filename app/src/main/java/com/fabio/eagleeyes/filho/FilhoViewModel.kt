package com.fabio.eagleeyes.filho

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.fabio.eagleeyes.funcionalidades.historico.HistoricoUso

import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilhoViewModel : ViewModel() {
    private val dao = FilhoDAO()

    fun salvarUsoApp(uidFilho: String, nome: String, tempo: Long) {
        // Agora o getCurrentDate() vai preencher a String 'data' do seu Model
        val historico = HistoricoUso(
            nomeApp = nome,
            tempoGasto = tempo,
            data = getCurrentDate()
        )

        viewModelScope.launch {
            dao.registrarAtividade(uidFilho, historico)
        }
    }

    // Esta função resolve o erro de "Unresolved reference"
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}