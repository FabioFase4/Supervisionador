package com.fabio.eagleeyes.filho.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.fabio.eagleeyes.repositorio.UsoRepositorio

class HomeFilhoViewModelFactory(private val repository: UsoRepositorio) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeFilhoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeFilhoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}