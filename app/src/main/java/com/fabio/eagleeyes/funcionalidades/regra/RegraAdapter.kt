package com.fabio.eagleeyes.funcionalidades.regra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.R

class RegraAdapter(
    private var regras: List<Regra>,
    private val onToggle: (Regra) -> Unit,
    private val onDelete: (Regra) -> Unit
) : RecyclerView.Adapter<RegraAdapter.RegraViewHolder>() {

    class RegraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtApp: TextView = view.findViewById(R.id.txtNomeAppRegra)
        val txtDetalhe: TextView = view.findViewById(R.id.txtDetalheRegra)
        val swAtiva: SwitchCompat = view.findViewById(R.id.swRegraAtiva)
        val btnDelete: ImageButton = view.findViewById(R.id.btnExcluirRegra)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegraViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_regra, parent, false)
        return RegraViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegraViewHolder, position: Int) {
        val regra = regras[position]
        
        holder.txtApp.text = if (regra.nomeApp.equals("Geral", true)) "Todos os Aplicativos" else regra.nomeApp
        
        // Formatação amigável do tipo e valor
        val detalhe = when (regra.tipo) {
            "BLOQUEIO" -> "Bloqueio Total"
            "LIMITE_TEMPO" -> "Limite: ${regra.valor} minutos/dia"
            "HORARIO" -> "Permitido entre: ${regra.valor.replace("-", " e ")}"
            else -> "${regra.tipo}: ${regra.valor}"
        }
        holder.txtDetalhe.text = detalhe
        
        // Previne trigger falso ao reciclar views
        holder.swAtiva.setOnCheckedChangeListener(null)
        holder.swAtiva.isChecked = regra.ativa
        
        holder.swAtiva.setOnCheckedChangeListener { _, isChecked ->
            if (regra.ativa != isChecked) {
                regra.ativa = isChecked
                onToggle(regra)
            }
        }

        holder.btnDelete.setOnClickListener { onDelete(regra) }
    }

    override fun getItemCount() = regras.size

    fun atualizarLista(novaLista: List<Regra>) {
        this.regras = novaLista
        notifyDataSetChanged()
    }
}
