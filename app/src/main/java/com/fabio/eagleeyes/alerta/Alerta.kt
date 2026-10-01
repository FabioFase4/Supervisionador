package com.fabio.eagleeyes.alerta

data class Alerta(
    val id: String = "",
    val aplicativo: String = "",
    val emailFilho: String = "",
    val horario: String = "",
    val mensagem: String = "",
    val tipo: String = "",
    val timestamp: Long = 0L
)