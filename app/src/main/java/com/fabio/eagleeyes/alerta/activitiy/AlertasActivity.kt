package com.fabio.eagleeyes.alerta.activitiy

import com.fabio.eagleeyes.global.BaseAlertaActivity

/**
 * Tela de visualização de todos os alertas (Visão do Pai).
 */
open class AlertasActivity : BaseAlertaActivity() {

    override fun setupSpecificLogic() {
        configurarTitulo("Todos os Alertas")
        viewModel.carregarAlertas()
    }
}
