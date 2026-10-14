package com.fabio.eagleeyes.vinculo

object VinculoPolicy {
    fun normalizarId(valor: String): String {
        val id = valor.trim()
        require(id.isNotEmpty()) { "Informe o ID da outra conta." }
        require(id.length <= 128 && id.none { it.isWhitespace() || it.code < 32 || it.code == 127 || it in ".#$[]/" }) {
            "ID inválido. Copie o ID na tela Vincular contas da outra pessoa; não use o e-mail."
        }
        return id // UIDs diferenciam maiúsculas de minúsculas.
    }

    fun podeVincular(atual: String?, responsavel: String): Boolean =
        atual.isNullOrBlank() || atual == responsavel
}
