package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Categoria

interface OnCategoriaClickListener {
    fun onCategoriaClick(categoria: Categoria)
}

class CategoriaAdapter(
    private val categorias: List<Categoria>,
    private val listener: OnCategoriaClickListener
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.categoria_list, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.nombreCategoria.text = categoria.nombre
        // Capturar el clic en la categoría
        holder.itemView.setOnClickListener {
            listener.onCategoriaClick(categoria)
        }
        Glide.with(holder.imageCategoria)
            .load(categoria.imgPlato)
            .placeholder(R.drawable.icon_platofuerte)
            .error(R.drawable.error_icon)
            .into(holder.imageCategoria)
    }

    override fun getItemCount(): Int = categorias.size

    inner class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCategoria: ImageView = itemView.findViewById(R.id.image_categoria)
        val nombreCategoria: TextView = itemView.findViewById(R.id.nombre_categoriaData)
    }
}



