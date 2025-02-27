package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.Datos
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.service.RetrofitService
import com.example.myapplication.viewmodel.CarritoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarritoAdapter(
    private var lista: MutableList<DetallePedido>,
    private val carritoViewModel: CarritoViewModel,
    private val retrofitService: RetrofitService,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    private val platosData = mutableListOf<Datos?>()

    init {
        cargarDatosPlatos()
    }

    private fun cargarDatosPlatos() {
        platosData.clear()
        lista.forEach { detalle ->
            platosData.add(null)
        }
        lista.forEachIndexed { index, detalle ->
            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val plato = retrofitService.getPlatoById(detalle.plato)
                    withContext(Dispatchers.Main) {
                        platosData[index] = plato
                        notifyItemChanged(index)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        platosData[index] = null
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }

    inner class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtNombrePlato: TextView = view.findViewById(R.id.txt_nombre_plato)
        private val txtPrecio: TextView = view.findViewById(R.id.txt_precio)
        private val btnEliminar: Button = view.findViewById(R.id.btn_eliminar)
        private val btnMas: Button = view.findViewById(R.id.btn_mas)
        private val btnMenos: Button = view.findViewById(R.id.btn_menos)
        private val txtCantidad: EditText = view.findViewById(R.id.txt_cantidad)
        private val imagePlato: ImageView = view.findViewById(R.id.imagen_plato_pedido)

        fun bind(detalle: DetallePedido) {
            val plato = platosData.getOrNull(adapterPosition)

            txtNombrePlato.text = plato?.name ?: "Plato desconocido"
            txtPrecio.text = "$${detalle.precio_total}"
            txtCantidad.setText(detalle.cantidad.toString())

            Glide.with(itemView.context)
                .load(plato?.imgPlato)
                .placeholder(R.drawable.icon_platofuerte)
                .error(R.drawable.error_icon)
                .into(imagePlato)

            btnMas.setOnClickListener {
                val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 1
                txtCantidad.setText((cantidad + 1).toString())
                carritoViewModel.actualizarCantidad(adapterPosition, cantidad + 1)
            }

            btnMenos.setOnClickListener {
                val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 1
                if (cantidad > 1) {
                    txtCantidad.setText((cantidad - 1).toString())
                    carritoViewModel.actualizarCantidad(adapterPosition, cantidad - 1)
                }
            }

            btnEliminar.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    lista.removeAt(position)
                    notifyItemRemoved(position)
                    carritoViewModel.eliminarDelCarrito(detalle)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_detalle_pedido, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<DetallePedido>) {
        lista.clear()
        lista.addAll(nuevaLista)
        cargarDatosPlatos()
    }
}