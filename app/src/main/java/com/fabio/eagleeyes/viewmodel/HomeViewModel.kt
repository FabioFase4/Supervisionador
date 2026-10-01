package com.fabio.eagleeyes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.repositorio.AnaliseRepositorio
import kotlinx.coroutines.launch

abstract class HomeViewModel(
    protected val analiseRepositorio: AnaliseRepositorio
) : ViewModel() {

    protected val _respostaIA = MutableLiveData<String>()
    val respostaIA: LiveData<String> get() = _respostaIA

    protected val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    protected val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    protected fun realizarAnaliseComIAParaPai(
        promptBase: String,
        onSuccess: (String) -> Unit = { resposta -> _respostaIA.value = resposta },
        onError: (String) -> Unit = { erro -> _erro.value = erro }
    ) {
        _carregando.value = true
        viewModelScope.launch {
            try {
                // Alterado para novaRequisicaoPai
                val resposta = analiseRepositorio.novaRequisicaoPai(promptBase)
                onSuccess(resposta)
            } catch (e: Exception) {
                onError("Erro na análise: ${e.localizedMessage}")
            } finally {
                _carregando.value = false
            }
        }
    }

    protected fun realizarAnaliseComIAParaFilho(
        promptBase: String,
        onSuccess: (String) -> Unit = { resposta -> _respostaIA.value = resposta },
        onError: (String) -> Unit = { erro -> _erro.value = erro }
    ) {
        _carregando.value = true
        viewModelScope.launch {
            try {
                // Alterado para novaRequisicaoFilho
                val resposta = analiseRepositorio.novaRequisicaoFilho(promptBase)
                onSuccess(resposta)
            } catch (e: Exception) {
                onError("Erro na análise: ${e.localizedMessage}")
            } finally {
                _carregando.value = false
            }
        }
    }
}