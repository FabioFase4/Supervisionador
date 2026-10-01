package com.fabio.eagleeyes.funcionalidades.regra

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GerenciarRegrasActivity : AppCompatActivity() {

    private lateinit var viewModel: RegraViewModel
    private lateinit var rvRegras: RecyclerView
    private lateinit var txtSemRegras: TextView
    private lateinit var adapter: RegraAdapter
    private var emailFilho: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerenciar_regras)

        emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""
        if (emailFilho.isEmpty()) {
            Toast.makeText(this, "Erro: Email do filho não identificado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupViewModel()
        setupObservers()
        
        // Inicia a observação em tempo real assim que a tela abre
        viewModel.iniciarObservacaoRegras(emailFilho)
    }

    private fun setupViews() {
        rvRegras = findViewById(R.id.rvRegras)
        txtSemRegras = findViewById(R.id.txtSemRegras)
        rvRegras.layoutManager = LinearLayoutManager(this)
        
        adapter = RegraAdapter(emptyList(),
            onToggle = { regra -> viewModel.alternarStatusRegra(regra) },
            onDelete = { regra -> viewModel.excluirRegra(regra) }
        )
        rvRegras.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddRegra).setOnClickListener {
            val intent = Intent(this, CriarRegraActivity::class.java)
            intent.putExtra("FILHO_EMAIL", emailFilho)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(RegraViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.listaRegras.observe(this) { lista ->
            adapter.atualizarLista(lista)
            
            // Gerencia a visibilidade do estado vazio
            if (lista.isNullOrEmpty()) {
                txtSemRegras.visibility = View.VISIBLE
                rvRegras.visibility = View.GONE
            } else {
                txtSemRegras.visibility = View.GONE
                rvRegras.visibility = View.VISIBLE
            }
        }

        viewModel.erro.observe(this) { mensagem ->
            if (mensagem.isNotEmpty()) {
                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Importante parar a observação para evitar vazamento de memória e consumo de dados desnecessário
        viewModel.pararObservacao()
    }
}
