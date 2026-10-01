package com.fabio.eagleeyes.filho.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.fabio.eagleeyes.filho.FilhosRepositorio
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.repositorio.AnaliseRepositorio
import com.fabio.eagleeyes.repositorio.UsoRepositorio
import com.fabio.eagleeyes.viewmodel.HomeViewModel

import kotlinx.coroutines.launch

class HomeFilhoViewModel(
    private val usoRepositorio: UsoRepositorio,
    private val filhosRepositorio: FilhosRepositorio = FilhosRepositorio(),
    analiseRepositorio: AnaliseRepositorio = AnaliseRepositorio()
) : HomeViewModel(analiseRepositorio) {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    init {
        val user = FirebaseConfig.getAuth().currentUser
        _userEmail.value = user?.email ?: ""
        _userName.value = user?.email?.substringBefore("@") ?: "Usuário"

        user?.uid?.let { carregarPerfil(it) }
    }

    private fun carregarPerfil(uid: String) {
        viewModelScope.launch {
            val nome = filhosRepositorio.buscarNomeFilho(uid)
            if (!nome.isNullOrEmpty()) {
                _userName.value = nome
            }
        }
    }

    fun realizarAnaliseIA() {
        val stats = usoRepositorio.buscarDadosUso()
        if (stats.isEmpty()) {
            _respostaIA.value = "Nenhum dado de uso encontrado nas últimas 24h."
            return
        }

        val resumo = stats.take(5).joinToString { "${it.first.substringAfterLast(".")}: ${usoRepositorio.formatarTempo(it.second)}" }
        val prompt = """
            Aja como um mentor de saúde digital.
            Com base no uso destes apps: $resumo
            Faça uma explicação rápida em, no máximo, 8 linhas.
        """.trimIndent()

        // Chama o método herdado da HomeViewModel
        realizarAnaliseComIAParaFilho(prompt)
    }
}