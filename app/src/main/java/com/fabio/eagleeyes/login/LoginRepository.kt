package com.fabio.eagleeyes.login

class LoginRepository(private val loginDAO: LoginDAO) {

    /**
     * Busca o tipo de usuário usando o UID.
     */
    suspend fun buscarTipoPorUid(uid: String): String? {
        return loginDAO.buscarTipoPorUid(uid)
    }

    /**
     * Mantido por compatibilidade, mas prefira buscar por UID.
     */
    suspend fun buscarUsuarioPorEmail(email: String): Pair<String?, String?> {
        val hashPai = loginDAO.buscarHashPorEmail("usuarios/pais", email)
        if (hashPai != null) {
            return Pair("pai", hashPai)
        }

        val hashFilho = loginDAO.buscarHashPorEmail("usuarios/filhos", email)
        if (hashFilho != null) {
            return Pair("filho", hashFilho)
        }

        return Pair(null, null)
    }
}