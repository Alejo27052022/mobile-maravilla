package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Datos

class PlatoAdapter(private val platos: List<Datos>, private val onAddClick: (Datos) -> Unit) : RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_pantalla_plato, parent, false)
        return PlatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        val plato = platos[position]
        holder.nombrePlato.text = plato.name
        holder.descripcionPlato.text = plato.descripcion
        holder.precioPlato.text = "$${plato.precio}"

        Glide.with(holder.imagenPlato)
            .load(plato.imgPlato)
            .placeholder(R.drawable.icon_platopostre)
            .error(R.drawable.error_icon)
            .into(holder.imagenPlato)

        holder.botonAgregar.setOnClickListener {
            onAddClick(plato) // Llama a la función de clic
        }
    }

    override fun getItemCount(): Int = platos.size

    inner class PlatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombrePlato: TextView = itemView.findViewById(R.id.nombre_plato_product)
        val descripcionPlato: TextView = itemView.findViewById(R.id.descrip_plato_product)
        val precioPlato: TextView = itemView.findViewById(R.id.precio_plato_product)
        val imagenPlato: ImageView = itemView.findViewById(R.id.imagen_plato_product)
        val botonAgregar: Button = itemView.findViewById(R.id.btn_add_product)
    }
}