package com.fabio.eagleeyes.global

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseConfig {

    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    fun getDatabase(): DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getAlertasRef(): DatabaseReference = getDatabase().child("alertas")
    
    // Caminhos corrigidos para refletir a estrutura 'usuarios/filhos' e 'usuarios/pais'
    fun getFilhosRef(): DatabaseReference = getDatabase().child("usuarios").child("filhos")
    fun getPaisRef(): DatabaseReference = getDatabase().child("usuarios").child("pais")

    fun getBloqueiosRef(): DatabaseReference = getDatabase().child("config_protecao")

    fun getRegrasRef(): DatabaseReference = getDatabase().child("regras")

    // Novo nó separado para histórico de uso
    fun getHistoricoUsoRef(): DatabaseReference = getDatabase().child("historicoUso")
}
