package com.fabio.eagleeyes.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.databinding.ItemFilhoBinding
import com.fabio.eagleeyes.usuario.Usuario
import com.fabio.eagleeyes.filho.detalhes.DetalhesFilhoActivity

class FilhoAdapter(private var lista: MutableList<Usuario>) :
    RecyclerView.Adapter<FilhoAdapter.FilhoViewHolder>() {

    class FilhoViewHolder(val binding: ItemFilhoBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateList(novaLista: List<Usuario>) {
        this.lista.clear()
        this.lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilhoViewHolder {
        val binding = ItemFilhoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilhoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilhoViewHolder, position: Int) {
        val filho = lista[position]
        
        with(holder.binding) {
            txtNomeFilho.text = filho.nome
            txtEmailFilho.text = filho.email

            root.setOnClickListener {
                val intent = Intent(root.context, DetalhesFilhoActivity::class.java).apply {
                    putExtra("FILHO_NOME", filho.nome)
                    putExtra("FILHO_EMAIL", filho.email)
                    putExtra("FILHO_UID", filho.uid)
                }
                root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = lista.size
}
