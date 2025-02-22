package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.Categoria

class CategoriaAdapter(private val categoria: List<Categoria>):
        RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.categoria_list, parent, false)
            return CategoriaViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
            holder.nombre_categoriaData.text = categoria[position].nombre
        }

        override fun getItemCount(): Int = categoria.size


        inner class CategoriaViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview){
            val nombre_categoriaData: TextView = itemview.findViewById(R.id.nombre_categoriaData)
        }
    }


