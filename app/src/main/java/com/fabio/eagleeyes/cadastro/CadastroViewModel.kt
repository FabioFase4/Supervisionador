package com.fabio.eagleeyes.cadastro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.usuario.UsuarioFilho
import com.fabio.eagleeyes.usuario.UsuarioPai
import com.fabio.eagleeyes.usuario.UsuarioRepositorio
import kotlinx.coroutines.launch

class CadastroViewModel(private val repositorio: UsuarioRepositorio) : ViewModel() {

    private val _carregando = MutableLiveData(false)
    val carregando: LiveData<Boolean> = _carregando

    private val _sucesso = MutableLiveData<String?>()
    val sucesso: LiveData<String?> = _sucesso

    private val _erro = MutableLiveData<String?>()
    val erro: LiveData<String?> = _erro

    fun cadastrarPai(usuario: UsuarioPai) {
        cadastrar(usuario, "Pai", "usuarios/pais")
    }

    fun cadastrarFilho(usuario: UsuarioFilho) {
        cadastrar(usuario, "Filho", "usuarios/filhos")
    }

    private fun cadastrar(usuario: Usuario, tipo: String, path: String) {
        viewModelScope.launch {
            _carregando.value = true
            _erro.value = null
            try {
                val uid = repositorio.cadastrar(usuario, path)
                if (uid != null) {
                    _sucesso.value = tipo
                } else {
                    _erro.value = "Erro ao cadastrar $tipo. Verifique os dados."
                }
            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro desconhecido ao cadastrar $tipo"
            } finally {
                _carregando.value = false
            }
        }
    }

    fun limparSucesso() {
        _sucesso.value = null
    }
}
