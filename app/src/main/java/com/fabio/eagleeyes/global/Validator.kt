package com.fabio.eagleeyes.global

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

object Validator {

    fun validarCampoVazio(inputLayout: TextInputLayout): Boolean {
        val texto = inputLayout.editText?.text.toString().trim()
        return if (texto.isEmpty()) {
            inputLayout.error = "Este campo é obrigatório"
            false
        } else {
            inputLayout.error = null
            true
        }
    }

    fun validarEmail(inputLayout: TextInputLayout): Boolean {
        val email = inputLayout.editText?.text.toString().trim()
        return when {
            email.isEmpty() -> {
                inputLayout.error = "E-mail é obrigatório"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                inputLayout.error = "E-mail inválido"
                false
            }
            else -> {
                inputLayout.error = null
                true
            }
        }
    }

    fun validarSenha(inputLayout: TextInputLayout): Boolean {
        val senha = inputLayout.editText?.text.toString().trim()
        return when {
            senha.isEmpty() -> {
                inputLayout.error = "Senha é obrigatória"
                false
            }
            senha.length < 6 -> {
                inputLayout.error = "A senha deve ter pelo menos 6 caracteres"
                false
            }
            else -> {
                inputLayout.error = null
                true
            }
        }
    }

    fun validarCPF(inputLayout: TextInputLayout): Boolean {
        val cpf = inputLayout.editText?.text.toString().trim()
        // Validação simples de tamanho, pode ser expandida para algoritmo real de CPF
        return when {
            cpf.isEmpty() -> {
                inputLayout.error = "CPF é obrigatório"
                false
            }
            cpf.length != 11 -> {
                inputLayout.error = "CPF deve ter 11 dígitos"
                false
            }
            else -> {
                inputLayout.error = null
                true
            }
        }
    }
}
