package com.example.myapplication.adaptadores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Datos
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.viewmodel.CarritoViewModel

class CategoriaPlatoAdapter(private val platos: List<Datos>, private val context: Context) :
    RecyclerView.Adapter<CategoriaPlatoAdapter.PlatoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_pantalla_categoria, parent, false)
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

        holder.botonAgregar.setOnClickListener {
            // Usar el contexto pasado desde la actividad
            val carritoViewModel = ViewModelProvider(context as ComponentActivity).get(CarritoViewModel::class.java)

            val detallePedido = DetallePedido(
                cantidad = 1,
                precio_total = plato.precio,
                pedido = 1,
                plato = plato.id
            )

            carritoViewModel.agregarAlCarrito(detallePedido, plato.name)
            carritoViewModel.actualizarPrecioTotal()

            Toast.makeText(context, "${plato.name} agregado", Toast.LENGTH_SHORT).show()
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