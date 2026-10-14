package com.fabio.eagleeyes.pai.home

import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.vinculo.VinculoRepository
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class HomePaiDAO(private val database: DatabaseReference = FirebaseConfig.getDatabase()) {
    suspend fun buscarPai(uid: String): Usuario? =
        database.child("usuarios/pais").child(uid).get().await().getValue(Usuario::class.java)?.apply { this.uid = uid }

    fun observarFilhos(uidPai: String) = VinculoRepository(database).observarFilhos(uidPai)
}
