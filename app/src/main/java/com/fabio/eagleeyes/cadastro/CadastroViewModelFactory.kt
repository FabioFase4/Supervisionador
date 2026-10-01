package com.fabio.eagleeyes.cadastro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.usuario.UsuarioRepositorio

class CadastroViewModelFactory(private val repositorio: UsuarioRepositorio) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CadastroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CadastroViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}
