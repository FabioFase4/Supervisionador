package com.fabio.eagleeyes.usuario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.global.FirebaseConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UsuarioViewModel : ViewModel() {

    // Canais de comunicação assíncrona com as Activities (Observers)
    private val _sucesso = MutableLiveData<String?>()
    val sucesso: LiveData<String?> get() = _sucesso

    private val _erro = MutableLiveData<String>()
    val erro: LiveData<String> get() = _erro

    // LiveData configurado como ArrayList para casar com o seu FilhoAdapter
    private val _listaFilhos = MutableLiveData<ArrayList<Usuario>>()
    val listaFilhos: LiveData<ArrayList<Usuario>> get() = _listaFilhos

    // Instância centralizada do Firebase Realtime Database
    private val database = FirebaseConfig.getDatabase()

    /**
     * 1. FUNÇÃO DE CADASTRO
     * Salva o usuário no Firebase Realtime Database
     */
    fun cadastrar(usuario: Usuario, caminho: String) {
        viewModelScope.launch {
            try {
                // Sanitiza o email para usar como chave no Firebase (não permite pontos)
                val emailChave = usuario.email.replace(".", ",")
                database.child(caminho).child(emailChave).setValue(usuario).await()

                _sucesso.value = caminho
            } catch (e: Exception) {
                _erro.value = "Erro ao cadastrar: ${e.localizedMessage}"
            }
        }
    }

    /**
     * 3. FUNÇÃO DE CARREGAR FILHOS
     * Busca no banco de dados os filhos vinculados ao e-mail do pai logado
     */
    fun carregarFilhos(emailPai: String) {
        viewModelScope.launch {
            try {
                // CORREÇÃO: Usando o caminho 'usuarios/filhos' e o campo 'emailResponsavel'
                val snapshot = database.child("usuarios/filhos")
                    .orderByChild("emailResponsavel")
                    .equalTo(emailPai)
                    .get().await()

                val lista = ArrayList<Usuario>()

                if (snapshot.exists()) {
                    for (filhoSnapshot in snapshot.children) {
                        val childUsuario = filhoSnapshot.getValue(Usuario::class.java)
                        if (childUsuario != null) {
                            lista.add(childUsuario)
                        }
                    }
                }

                _listaFilhos.value = lista

            } catch (e: Exception) {
                _erro.value = "Erro ao buscar dados dos filhos: ${e.localizedMessage}"
            }
        }
    }

    fun limparEstado() {
        _sucesso.value = null
        _erro.value = ""
    }
}