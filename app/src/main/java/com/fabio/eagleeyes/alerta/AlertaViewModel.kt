package com.fabio.eagleeyes.alerta

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlertaViewModel (private val repositorio: AlertaRepositorio): ViewModel() {
    private val _alertas = MutableLiveData<List<Alerta>>()
    val alertas: LiveData<List<Alerta>> = _alertas

    /**
     * Inicia a escuta em tempo real dos alertas através do repositório.
     * @param emailFilho Opcional. Se fornecido, filtra alertas apenas deste filho.
     */
    fun carregarAlertas(emailFilho: String? = null) {
        viewModelScope.launch {
            repositorio.escutarAlertas(emailFilho)
                .catch { erro ->
                    Log.e("AlertaViewModel", "Erro ao carregar alertas: ${erro.message}")
                }
                .collect { listaDeAlertas ->
                    _alertas.value = listaDeAlertas
                }
        }
    }

    fun salvarAlerta (alerta: Alerta) {
        viewModelScope.launch {
            repositorio.salvarAlerta(alerta)
        }
    }
}
