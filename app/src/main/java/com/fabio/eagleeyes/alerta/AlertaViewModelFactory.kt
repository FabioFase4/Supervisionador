package com.fabio.eagleeyes.alerta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AlertaViewModelFactory(private val repositorio: AlertaRepositorio) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlertaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("ViewModel desconhecido")
    }
}