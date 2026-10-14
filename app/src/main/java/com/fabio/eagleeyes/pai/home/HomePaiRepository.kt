package com.fabio.eagleeyes.pai.home

class HomePaiRepository(private val dao: HomePaiDAO) {
    suspend fun buscarPai(uid: String) = dao.buscarPai(uid)
    fun observarFilhos(uidPai: String) = dao.observarFilhos(uidPai)
}
