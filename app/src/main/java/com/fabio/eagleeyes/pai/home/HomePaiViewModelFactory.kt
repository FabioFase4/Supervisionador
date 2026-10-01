package com.fabio.eagleeyes.pai.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomePaiViewModelFactory(private val repository: HomePaiRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePaiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomePaiViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}
