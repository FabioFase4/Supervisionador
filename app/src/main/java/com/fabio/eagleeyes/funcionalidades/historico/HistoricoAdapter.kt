package com.fabio.eagleeyes.funcionalidades.historico

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.fabio.eagleeyes.R

class HistoricoAdapter(private val lista: List<HistoricoUso>):
    RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder>() {

    class HistoricoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Vinculando aos campos corretos do HistoricoUso
        val txtNomeApp: TextView = view.findViewById(R.id.txtDadosBrutos)
        val txtDetalhes: TextView = view.findViewById(R.id.txtTimeStamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoricoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historico, parent, false)
        return HistoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoricoViewHolder, position: Int) {
        val historico = lista[position]
        
        // Exibe o nome do aplicativo
        holder.txtNomeApp.text = historico.nomeApp
        
        // Exibe a data e o tempo gasto formatado
        val detalhes = "Data: ${historico.data} | Uso: ${historico.tempoGasto} min"
        holder.txtDetalhes.text = detalhes
    }

    override fun getItemCount() = lista.size
}