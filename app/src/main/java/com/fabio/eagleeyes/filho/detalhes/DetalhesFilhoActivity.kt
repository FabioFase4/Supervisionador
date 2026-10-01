package com.fabio.eagleeyes.filho.detalhes

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.fabio.eagleeyes.alerta.activitiy.AlertasFilhoActivity
import com.fabio.eagleeyes.databinding.ActivityDetalhesFilhoBinding
import com.fabio.eagleeyes.funcionalidades.historico.RelatorioFilhoActivity
import com.fabio.eagleeyes.funcionalidades.regra.GerenciarRegrasActivity

class DetalhesFilhoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalhesFilhoBinding
    private lateinit var viewModel: DetalhesFilhoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityDetalhesFilhoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DetalhesFilhoViewModel::class.java]

        val nomeFilho = intent.getStringExtra("FILHO_NOME") ?: "Filho"
        val emailFilho = intent.getStringExtra("FILHO_EMAIL") ?: ""
        val uidFilho = intent.getStringExtra("FILHO_UID") ?: ""

        setupUI(nomeFilho, emailFilho)
        setupCliques(nomeFilho, emailFilho, uidFilho)
        setupObservers()
    }

    private fun setupUI(nome: String, email: String) {
        binding.txtNomeDetalhes.text = nome
        binding.txtEmailDetalhes.text = email
    }

    private fun setupCliques(nome: String, email: String, uid: String) {
        binding.apply {
            btnVerHistorico.setOnClickListener {
                startActivity(Intent(this@DetalhesFilhoActivity, RelatorioFilhoActivity::class.java).apply {
                    putExtra("FILHO_UID", uid)
                    putExtra("FILHO_NOME", nome)
                })
            }

            btnVerAlertasFilho.setOnClickListener {
                startActivity(Intent(this@DetalhesFilhoActivity, AlertasFilhoActivity::class.java).apply {
                    putExtra("FILHO_UID", uid)
                    putExtra("FILHO_EMAIL", email) // Passando o email corretamente aqui!
                    putExtra("FILHO_NOME", nome)
                })
            }

            btnGerenciarRegras.setOnClickListener {
                startActivity(Intent(this@DetalhesFilhoActivity, GerenciarRegrasActivity::class.java).apply {
                    putExtra("FILHO_UID", uid)
                    putExtra("FILHO_EMAIL", email)
                })
            }

            btnVoltarDetalhes.setOnClickListener {
                finish()
            }
        }
    }

    private fun setupObservers() {
        viewModel.erro.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
