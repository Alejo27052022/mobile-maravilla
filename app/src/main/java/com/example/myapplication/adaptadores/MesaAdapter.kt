package com.example.myapplication.adaptadores

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Mesa

class MesaAdapter(private val mesas: List<Mesa>, private val onMesaClick: (Mesa) -> Unit) :
    RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    inner class MesaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtNumeroMesa: TextView = view.findViewById(R.id.txtNumeroMesa)

        fun bind(mesa: Mesa) {
            txtNumeroMesa.text = mesa.numero_mesa.toString()

            // Cambiar el fondo según el estado de la mesa
            if (mesa.estado_mesa == "disponible") {
                txtNumeroMesa.setBackgroundResource(R.drawable.fondo_green)
                itemView.isEnabled = true // Habilitar la vista si la mesa está disponible
            } else if (mesa.estado_mesa == "ocupada") {
                txtNumeroMesa.setBackgroundResource(R.drawable.fondo_red)
                itemView.isEnabled = false // Deshabilitar la vista si la mesa está ocupada
            }

            itemView.setOnClickListener {
                if (itemView.isEnabled) { // Verificar si la vista está habilitada antes de llamar a onMesaClick
                    onMesaClick(mesa)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mesa_list, parent, false)
        return MesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        holder.bind(mesas[position])
    }

    override fun getItemCount(): Int = mesas.size
}