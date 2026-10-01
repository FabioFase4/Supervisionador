package com.fabio.eagleeyes.alerta.activitiy

import com.fabio.eagleeyes.global.BaseAlertaActivity

/**
 * Tela de visualização de alertas específicos de um filho (Visão do Filho ou detalhe do Pai).
 */
class AlertasFilhoActivity : BaseAlertaActivity() {

    override fun setupSpecificLogic() {
        val emailFilho = intent.getStringExtra("FILHO_EMAIL")
        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Meus Alertas"
        
        val titulo = if (nomeFilho == "Meus Alertas") nomeFilho else "Alertas de $nomeFilho"
        configurarTitulo(titulo)

        viewModel.carregarAlertas(emailFilho)
    }
}
