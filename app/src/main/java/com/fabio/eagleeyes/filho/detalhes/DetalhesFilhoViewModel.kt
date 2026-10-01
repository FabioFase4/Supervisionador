package com.fabio.eagleeyes.filho.detalhes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.filho.FilhosRepositorio
import com.fabio.eagleeyes.funcionalidades.historico.HistoricoUso
import kotlinx.coroutines.launch

class DetalhesFilhoViewModel(private val repository: FilhosRepositorio = FilhosRepositorio()) : ViewModel() {

    private val _historico = MutableLiveData<List<HistoricoUso>>()
    val historico: LiveData<List<HistoricoUso>> get() = _historico

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    fun carregarHistorico(identificadorFilho: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val lista = repository.buscarHistorico(identificadorFilho)
                _historico.value = lista
            } catch (e: Exception) {
                _erro.value = "Erro ao carregar histórico: ${e.localizedMessage}"
                _historico.value = emptyList()
            } finally {
                _carregando.value = false
            }
        }
    }

    fun gerarDadosTeste(emailFilho: String) {
        viewModelScope.launch {
            try {
                val sucesso = repository.gerarHistoricoTeste(emailFilho)
                if (sucesso) {
                    carregarHistorico(emailFilho)
                } else {
                    _erro.value = "Falha ao gerar dados de teste."
                }
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.localizedMessage}"
            }
        }
    }
}