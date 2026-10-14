package com.fabio.eagleeyes.pai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.fabio.eagleeyes.adapter.FilhoAdapter
import com.fabio.eagleeyes.databinding.ActivityListaFilhosBinding
import com.fabio.eagleeyes.global.FirebaseConfig
import com.fabio.eagleeyes.pai.home.HomePaiDAO
import com.fabio.eagleeyes.pai.home.HomePaiRepository
import com.fabio.eagleeyes.pai.home.HomePaiViewModel
import com.fabio.eagleeyes.pai.home.HomePaiViewModelFactory
import com.fabio.eagleeyes.vinculo.VinculoActivity

class ListaFilhosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaFilhosBinding
    private lateinit var viewModel: HomePaiViewModel
    private lateinit var adapter: FilhoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityListaFilhosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupArchitecture()
        setupRecyclerView()
        setupObservers()
        setupCliques()

        // Carrega a lista de filhos vinculados a este pai
        val uid = FirebaseConfig.getAuth().currentUser?.uid
        if (uid == null) finish() else viewModel.carregarFilhos(uid)
    }

    private fun setupArchitecture() {
        val database = FirebaseConfig.getDatabase()
        val dao = HomePaiDAO(database)
        val repository = HomePaiRepository(dao)
        val factory = HomePaiViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HomePaiViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FilhoAdapter(mutableListOf())
        binding.rvFilhos.layoutManager = LinearLayoutManager(this)
        binding.rvFilhos.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.carregando.observe(this) { estaCarregando ->
            binding.progressBar.visibility = if (estaCarregando) View.VISIBLE else View.GONE
            binding.btnCadastrarFilho.isEnabled = !estaCarregando
        }

        viewModel.listaFilhos.observe(this) { filhos ->
            binding.txtListaVazia.text = "Nenhum filho vinculado. Toque em Adicionar filho pelo ID."
            adapter.updateList(filhos ?: emptyList())
            binding.txtListaVazia.visibility = if (filhos.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.erro.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                binding.txtListaVazia.text = msg
                binding.txtListaVazia.visibility = View.VISIBLE
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCliques() {
        binding.btnCadastrarFilho.setOnClickListener {
            startActivity(Intent(this, VinculoActivity::class.java))
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}
