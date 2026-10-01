package com.fabio.eagleeyes.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.global.FirebaseConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {

    private val _sucesso = MutableLiveData<String?>()
    val sucesso: LiveData<String?> = _sucesso

    private val _erro = MutableLiveData<String?>()
    val erro: LiveData<String?> = _erro

    private val _carregando = MutableLiveData<Boolean>()
    val carregando: LiveData<Boolean> = _carregando

    private val auth = FirebaseConfig.getAuth()

    fun logar(email: String, senhaDigitada: String) {
        val emailFormatado = email.lowercase().trim()

        if (emailFormatado.isBlank() || senhaDigitada.isBlank()) {
            _erro.value = "Preencha todos os campos"
            return
        }

        viewModelScope.launch {
            _carregando.value = true
            try {
                // 1. Autentica oficialmente no Firebase Auth
                val result = auth.signInWithEmailAndPassword(emailFormatado, senhaDigitada).await()
                val uid = result.user?.uid ?: throw Exception("ID do usuário não encontrado.")

                // 2. Busca o tipo de perfil (Pai ou Filho) usando o UID
                buscarPerfilPorUid(uid)
            } catch (e: Exception) {
                _erro.value = "Falha no Login: Verifique seus dados."
                _carregando.value = false
            }
        }
    }

    /**
     * Função para login automático quando o app abre e já existe um usuário logado
     */
    fun logarAutomaticamente(uid: String) {
        viewModelScope.launch {
            _carregando.value = true
            buscarPerfilPorUid(uid)
        }
    }

    private suspend fun buscarPerfilPorUid(uid: String) {
        try {
            val tipo = repository.buscarTipoPorUid(uid)
            if (tipo != null) {
                _sucesso.value = tipo
            } else {
                _erro.value = "Perfil não encontrado no banco de dados."
                auth.signOut()
            }
        } catch (e: Exception) {
            _erro.value = "Erro ao recuperar perfil: ${e.localizedMessage}"
        } finally {
            _carregando.value = false
        }
    }

    fun limparEstado() {
        _sucesso.value = null
        _erro.value = null
    }
}