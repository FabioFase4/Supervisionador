package com.fabio.eagleeyes.alerta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fabio.eagleeyes.databinding.ItemAlertaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertaAdapter(private var lista: List<Alerta>) : RecyclerView.Adapter<AlertaAdapter.AlertaViewHolder>() {

    /**
     * ViewHolder usando ViewBinding para acesso direto aos componentes do layout.
     */
    class AlertaViewHolder(val binding: ItemAlertaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val binding = ItemAlertaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        val alerta = lista[position]
        
        with(holder.binding) {
            txtAlertaFilho.text = "Filho: ${alerta.emailFilho}"
            txtAlertaMensagem.text = alerta.mensagem
            txtAlertaHorario.text = formatarData(alerta)
        }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Alerta>) {
        this.lista = novaLista
        notifyDataSetChanged()
    }

    /**
     * Lógica de formatação centralizada para deixar o onBindViewHolder limpo.
     */
    private fun formatarData(alerta: Alerta): String {
        return try {
            if (alerta.timestamp != 0L) {
                val datetime = Date(alerta.timestamp)
                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(datetime)
            } else if (alerta.horario.isNotEmpty()) {
                alerta.horario
            } else {
                "--:--"
            }
        } catch (e: Exception) {
            alerta.horario
        }
    }
}
