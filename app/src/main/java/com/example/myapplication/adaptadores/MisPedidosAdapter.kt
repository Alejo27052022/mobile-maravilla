package com.example.myapplication.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.MisDetallePedidos
import com.example.myapplication.data.MisPedidos
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MisPedidosAdapter(private val listaPedidos: List<MisPedidos>) :
    RecyclerView.Adapter<MisPedidosAdapter.MisPedidosViewHolder>() {

    class MisPedidosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numeroPedido: TextView = itemView.findViewById(R.id.txt_numero_pedido)
        val fechaPedido: TextView = itemView.findViewById(R.id.txt_fecha_pedido)
        val mesaPedido: TextView = itemView.findViewById(R.id.txt_numero_mesa_pedido)
        val estadoPedido: TextView = itemView.findViewById(R.id.txt_estado_pedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisPedidosViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mis_pedidos_list, parent, false)
        return MisPedidosViewHolder(view)
    }

    override fun onBindViewHolder(holder: MisPedidosViewHolder, position: Int) {
        val pedido = listaPedidos[position]

        // Formatear la fecha
        try {
            val dateTime = LocalDateTime.parse(pedido.fecha_entrega, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            holder.numeroPedido.text = pedido.id.toString()
            holder.fechaPedido.text = formattedDate
            holder.mesaPedido.text = pedido.mesa.toString()
            holder.estadoPedido.text = pedido.estado
        } catch (e: Exception) {
            // Manejar el error de formato de fecha
            holder.fechaPedido.text = "Fecha no disponible"
            holder.numeroPedido.text = pedido.id.toString()
            holder.mesaPedido.text = pedido.mesa.toString()
            holder.estadoPedido.text = pedido.estado
        }
    }

    override fun getItemCount(): Int = listaPedidos.size
}