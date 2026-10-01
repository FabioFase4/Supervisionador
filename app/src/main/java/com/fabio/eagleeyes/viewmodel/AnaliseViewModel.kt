package com.fabio.eagleeyes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.repositorio.AnaliseRepositorio
import com.fabio.eagleeyes.formatadores.MontarJSON
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AnaliseViewModel : ViewModel() {
    private val analiseRepositorio = AnaliseRepositorio()
    private val montarJSON: MontarJSON = MontarJSON()

    private val _respostaIA = MutableLiveData<String>()
    val respostaIA: LiveData<String> get() = _respostaIA

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> get() = _carregando

    // OkHttpClient centralizado com timeouts maiores para evitar erros com a IA
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun analisar(prompt: String, usarBackend: Boolean = true) {
        _carregando.value = true
        viewModelScope.launch {
            try {
                // Ajustado para usar novaRequisicaoPai, que é o contexto do RelatorioFilho
                _respostaIA.value = analiseRepositorio.novaRequisicaoPai(prompt, usarBackend)
            } catch (e: Exception) {
                _respostaIA.value = "Erro ao conectar com Gemini: ${e.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }
}