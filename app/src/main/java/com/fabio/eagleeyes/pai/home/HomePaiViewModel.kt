package com.fabio.eagleeyes.pai.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.repositorio.AnaliseRepositorio
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class HomePaiViewModel(
    private val repository: HomePaiRepository,
    analiseRepositorio: AnaliseRepositorio = AnaliseRepositorio()
) : HomeViewModel(analiseRepositorio) {

    private val _listaFilhos = MutableLiveData<List<Usuario>>()
    val listaFilhos: LiveData<List<Usuario>> get() = _listaFilhos

    private val _perfilPai = MutableLiveData<Usuario?>()
    val perfilPai: LiveData<Usuario?> get() = _perfilPai

    private var filhosJob: Job? = null

    fun carregarDadosIniciais(uid: String) {
        viewModelScope.launch {
            _carregando.value = true
            try {
                val pai = repository.buscarPai(uid)
                _perfilPai.value = pai
                
                carregarFilhos(uid)
            } catch (e: Exception) {
                _erro.value = "Erro ao carregar perfil: ${e.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }

    fun carregarFilhos(uidPai: String) {
        filhosJob?.cancel()
        filhosJob = viewModelScope.launch {
            _carregando.value = true
            try {
                repository.observarFilhos(uidPai).collect { filhos ->
                    _listaFilhos.value = filhos
                    _erro.value = ""
                    _carregando.value = false
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _erro.value = "Não foi possível buscar os filhos: ${error.localizedMessage}"
            } finally {
                _carregando.value = false
            }
        }
    }

    fun analisarComportamentoComIA(pergunta: String) {
        val filhos = _listaFilhos.value
        if (filhos.isNullOrEmpty()) {
            _erro.value = "Nenhum filho cadastrado para analisar."
            return
        }

        viewModelScope.launch {
            _carregando.value = true
            try {
                val resumoUso = coletarResumoUsoFilhos(filhos)
                val promptFinal = """
                    Você é o assistente EagleEyes.
                    Dados de uso do(s) filho(s):
                    $resumoUso

                    Pergunta do responsável: $pergunta

                    Responda de forma curta e prática.
                """.trimIndent()

                Log.d("IA_DEBUG", "Enviando prompt: $promptFinal")

                realizarAnaliseComIAParaPai(promptFinal)
            } catch (e: Exception) {
                _erro.value = "Erro na análise: ${e.message}"
            } finally {
                _carregando.value = false
            }
        }
    }

    private suspend fun coletarResumoUsoFilhos(filhos: List<Usuario>): String {
        val builder = StringBuilder()

        for (filho in filhos) {
            if (filho.uid.isEmpty()) continue

            try {
                val snapshot = FirebaseConfig.getFilhosRef()
                    .child(filho.uid)
                    .child("historicoUso")
                    .get()
                    .await()

                if (snapshot.exists()) {
                    builder.append("Filho(a) ${filho.nome}:\n")
                    snapshot.children.forEach { diaSnapshot ->
                        val data = diaSnapshot.key ?: "Data desconhecida"
                        val tempoTotalDia = diaSnapshot.children.sumOf { appSnapshot ->
                            appSnapshot.value as? Long ?: 0L
                        }
                        builder.append("- $data: ${formatarTempo(tempoTotalDia)}\n")
                    }
                    builder.append("\n")
                } else {
                    builder.append("Filho(a) ${filho.nome}: Sem dados.\n\n")
                }
            } catch (e: Exception) {
                builder.append("Filho(a) ${filho.nome}: Erro ao carregar.\n\n")
            }
        }
        return builder.toString().ifEmpty { "Nenhum dado disponível." }
    }

    private fun formatarTempo(millis: Long): String {
        val segundos = millis / 1000
        val horas = segundos / 3600
        val minutos = (segundos % 3600) / 60
        return when {
            horas > 0 -> "$horas h $minutos min"
            minutos > 0 -> "$minutos min"
            else -> "< 1 min"
        }
    }
}
