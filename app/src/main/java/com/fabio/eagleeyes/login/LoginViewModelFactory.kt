package com.fabio.eagleeyes.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.login.LoginRepository
import com.fabio.eagleeyes.login.LoginViewModel

class LoginViewModelFactory(private val repository: LoginRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}