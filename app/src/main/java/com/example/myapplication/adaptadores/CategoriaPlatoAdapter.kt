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

class CategoriaPlatoAdapter(private val platos: List<Datos>) :
    RecyclerView.Adapter<CategoriaPlatoAdapter.PlatoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_pantalla_categoria, parent, false)  // Asegúrate que el XML se llame igual
        return PlatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        val plato = platos[position]

        holder.nombrePlato.text = plato.name
        holder.descripcionPlato.text = plato.descripcion
        holder.precioPlato.text = "$${plato.precio}"
        Glide.with(holder.imagenPlato)
            .load(plato.imgPlato)
            .placeholder(R.drawable.icon_platofuerte)
            .error(R.drawable.error_icon)
            .into(holder.imagenPlato)

        // Aquí puedes agregar un `setOnClickListener` si deseas manejar eventos en los platos
        holder.botonAgregar.setOnClickListener {
            Toast.makeText(holder.itemView.context, "${plato.name} agregado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = platos.size

    inner class PlatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombrePlato: TextView = itemView.findViewById(R.id.nombre_plato)
        val descripcionPlato: TextView = itemView.findViewById(R.id.descripcion_plato)
        val precioPlato: TextView = itemView.findViewById(R.id.text_precio_plato)
        val imagenPlato: ImageView = itemView.findViewById(R.id.imagen_plato)
        val botonAgregar: Button = itemView.findViewById(R.id.agregar_pedido)
    }
}
