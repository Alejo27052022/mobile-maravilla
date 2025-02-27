package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class MesaAdapter(private val mesas: List<Int>, private val onMesaClick: (Int) -> Unit) : RecyclerView.Adapter<MesaAdapter.MesaViewHolder>() {

    inner class MesaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtNumeroMesa: TextView = view.findViewById(R.id.txtNumeroMesa)

        fun bind(mesa: Int) {
            txtNumeroMesa.text = "Mesa $mesa"
            itemView.setOnClickListener { onMesaClick(mesa) }
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