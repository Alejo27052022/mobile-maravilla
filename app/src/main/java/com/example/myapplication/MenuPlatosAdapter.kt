package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Datos

class MenuPlatosAdapter(private val platos: List<Datos>):
    RecyclerView.Adapter<MenuPlatosAdapter.PlatosViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatosViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)
            return PlatosViewHolder(view)
        }

        override fun onBindViewHolder(holder: PlatosViewHolder, position: Int){
            holder.nombre_platoData.text = platos[position].name
        }

    override fun getItemCount(): Int = platos.size

        inner class PlatosViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview){
            val nombre_platoData: TextView = itemview.findViewById(R.id.text1)
        }
    }


