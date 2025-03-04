package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.MisPedidosAdapter
import com.example.myapplication.viewmodel.MisPedidosViewModel

class Perfil : ComponentActivity() {

    private lateinit var misPedidosViewModel: MisPedidosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_profile)

        val iconhome : ImageView = findViewById(R.id.icon_home)
        iconhome.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }

        val iconImage: ImageView = findViewById(R.id.icon_profile)
        iconImage.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }
        iconImage.isSelected = true

        // ViewModel y RecyclerView
        misPedidosViewModel = ViewModelProvider(this)[MisPedidosViewModel::class.java]
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_mis_pedidos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        misPedidosViewModel.misPedidos.observe(this) { pedidos ->
            // Filtrar pedidos por estado "pendiente" o "en camino"
            val pedidosFiltrados = pedidos.filter { it.estado == "pendiente" || it.estado == "en camino" }
            val adapter = MisPedidosAdapter(pedidosFiltrados)
            recyclerView.adapter = adapter
        }

        misPedidosViewModel.obtenerMisPedidos()
    }
}