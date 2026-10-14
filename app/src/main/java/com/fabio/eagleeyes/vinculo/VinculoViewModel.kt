package com.fabio.eagleeyes.vinculo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VinculoState(val perfil: PerfilVinculo? = null, val carregando: Boolean = false, val mensagem: String? = null)

class VinculoViewModel : ViewModel() {
    private val repository = VinculoRepository()
    private val mutableState = MutableStateFlow(VinculoState())
    val state = mutableState.asStateFlow()

    fun carregar() {
        if (state.value.carregando) return
        executar { mutableState.value = state.value.copy(perfil = repository.perfil()) }
    }

    fun vincular(id: String) {
        if (state.value.carregando || state.value.perfil == null) return
        executar {
            repository.vincular(id)
            mutableState.value = state.value.copy(perfil = repository.perfil(), mensagem = "Contas vinculadas com sucesso.")
        }
    }

    private fun executar(action: suspend () -> Unit) {
        mutableState.value = state.value.copy(carregando = true, mensagem = null)
        viewModelScope.launch {
            try { action() }
            catch (error: CancellationException) { throw error }
            catch (error: Exception) {
                mutableState.value = state.value.copy(mensagem = "Não foi possível concluir: ${error.localizedMessage}")
            } finally { mutableState.value = state.value.copy(carregando = false) }
        }
    }
}
