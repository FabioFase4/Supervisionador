package com.fabio.eagleeyes.funcionalidades.regra

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.filho.FilhosRepositorio
import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class RegraViewModel(private val repository: FilhosRepositorio = FilhosRepositorio()) : ViewModel() {

    private val _listaRegras = MutableLiveData<List<Regra>>()
    val listaRegras: LiveData<List<Regra>> get() = _listaRegras

    private val _sucesso = MutableLiveData<Boolean>()
    val sucesso: LiveData<Boolean> get() = _sucesso

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    private var currentListener: ValueEventListener? = null
    private var currentPath: String? = null

    fun iniciarObservacaoRegras(emailFilho: String) {
        val emailChave = emailFilho.replace(".", ",")
        val ref = FirebaseConfig.getDatabase().child("regras").child(emailChave)

        // Remove listener anterior se existir
        pararObservacao()

        currentPath = emailChave
        currentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Regra>()
                for (item in snapshot.children) {
                    item.getValue(Regra::class.java)?.let { lista.add(it) }
                }
                _listaRegras.postValue(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                _erro.postValue("Erro de sincronização: ${error.message}")
            }
        }

        ref.addValueEventListener(currentListener!!)
    }

    fun salvarRegra(regra: Regra) {
        if (regra.nomeApp.isEmpty() || regra.valor.isEmpty()) {
            _erro.value = "Preencha todos os campos!"
            return
        }

        viewModelScope.launch {
            try {
                val result = repository.salvarRegra(regra)
                if (result) _sucesso.value = true
                else _erro.value = "Erro ao salvar no banco."
            } catch (e: Exception) {
                _erro.value = e.localizedMessage ?: "Erro desconhecido."
            }
        }
    }

    fun alternarStatusRegra(regra: Regra) {
        viewModelScope.launch {
            try {
                repository.salvarRegra(regra)
            } catch (e: Exception) {
                _erro.value = "Erro ao atualizar: ${e.localizedMessage}"
            }
        }
    }

    fun excluirRegra(regra: Regra) {
        viewModelScope.launch {
            try {
                repository.excluirRegra(regra.emailFilho, regra.id)
            } catch (e: Exception) {
                _erro.value = "Erro ao excluir: ${e.localizedMessage}"
            }
        }
    }

    fun pararObservacao() {
        currentListener?.let { listener ->
            currentPath?.let { path ->
                FirebaseConfig.getDatabase().child("regras").child(path).removeEventListener(listener)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pararObservacao()
    }
}
