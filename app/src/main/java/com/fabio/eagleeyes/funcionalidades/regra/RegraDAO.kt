package com.fabio.eagleeyes.funcionalidades.regra

import com.fabio.eagleeyes.global.FirebaseConfig
import com.google.firebase.database.DatabaseReference

class RegraDAO {
    private val regrasRef = FirebaseConfig.getRegrasRef()

    fun noRegrasFilho (uidFilho: String): DatabaseReference {
        return regrasRef.child(uidFilho)
    }
}
