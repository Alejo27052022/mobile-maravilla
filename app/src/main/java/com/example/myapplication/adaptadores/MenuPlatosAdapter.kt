package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Datos

class MenuPlatosAdapter(private val platos: List<Datos>):
    RecyclerView.Adapter<MenuPlatosAdapter.PlatosViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatosViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.platos_list, parent, false)
            return PlatosViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlatosViewHolder, position: Int){
            val plato = platos[position]
            holder.nombre_platoData.text = plato.name
            Glide.with(holder.image_platoData)
                .load(plato.imgPlato)
                .placeholder(R.drawable.icon_platofuerte)
                .error(R.drawable.error_icon)
                .into(holder.image_platoData)

            holder.precio_platoData.text = "$${plato.precio}"
        }

        override fun getItemCount(): Int = platos.size

        inner class PlatosViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview){
            val nombre_platoData: TextView = itemview.findViewById(R.id.nombre_plato)
            val precio_platoData: TextView = itemview.findViewById(R.id.precio_plato)
            val image_platoData: ImageView = itemview.findViewById(R.id.image_plato)
        }
    }


