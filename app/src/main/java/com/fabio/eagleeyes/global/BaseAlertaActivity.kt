package com.fabio.eagleeyes.global

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fabio.eagleeyes.alerta.AlertaAdapter
import com.fabio.eagleeyes.alerta.AlertaRepositorio
import com.fabio.eagleeyes.alerta.AlertaViewModel
import com.fabio.eagleeyes.alerta.AlertaViewModelFactory
import com.fabio.eagleeyes.databinding.ActivityVisualizarAlertasBinding

/**
 * Base Activity para as telas de Alertas.
 * Centraliza a configuração do RecyclerView, ViewModel e Observers básicos.
 */
abstract class BaseAlertaActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityVisualizarAlertasBinding
    protected lateinit var viewModel: AlertaViewModel
    protected lateinit var adapter: AlertaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisualizarAlertasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupBaseObservers()
        setupSpecificLogic()

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        val repositorio = AlertaRepositorio()
        val factory = AlertaViewModelFactory(repositorio)
        viewModel = ViewModelProvider(this, factory)[AlertaViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = AlertaAdapter(emptyList())
        binding.rvAlertas.layoutManager = LinearLayoutManager(this)
        binding.rvAlertas.adapter = adapter
    }

    private fun setupBaseObservers() {
        viewModel.alertas.observe(this) { lista ->
            lista?.let {
                adapter.atualizarLista(it)
            }
        }

        // Caso exista um estado de carregamento no AlertaViewModel
        try {
            val carregandoField = viewModel.javaClass.getDeclaredField("carregando")
            // Se for um LiveData, poderíamos observar aqui. 
            // Mas vamos manter simples baseado no que vi na AlertasActivity original.
        } catch (e: Exception) { }
    }

    abstract fun setupSpecificLogic()
    
    protected fun configurarTitulo(titulo: String) {
        binding.txtTituloAlertas.text = titulo
    }
}
