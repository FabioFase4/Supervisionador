package com.fabio.eagleeyes.funcionalidades.regra

data class Regra(
    var id: String = "",
    var emailFilho: String = "",
    var nomeApp: String = "",        // Ex: "YouTube" ou "Geral"
    var tipo: String = "",           // Ex: "LIMITE_TEMPO", "BLOQUEIO", "HORARIO"
    var valor: String = "",          // Ex: "60" (minutos) ou "22:00"
    var ativa: Boolean = true
)