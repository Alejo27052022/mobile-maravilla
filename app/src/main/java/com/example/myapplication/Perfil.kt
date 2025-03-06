package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.MisPedidosAdapter
import com.example.myapplication.data.Clientes
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.service.RetrofitService
import com.example.myapplication.viewmodel.MisPedidosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Perfil : ComponentActivity() {

    private lateinit var retrofitService: RetrofitService
    private lateinit var misPedidosViewModel: MisPedidosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_profile)

        retrofitService = RetrofitInstance.getRetrofitAuth(this)

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
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cliente: Clientes = retrofitService.getUserProfile()
                withContext(Dispatchers.Main) {
                    updateUI(cliente)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PerfilActivity", "Error fetching user profile", e)
                    Toast.makeText(this@Perfil, "Error fetching user profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(cliente: Clientes) {
        findViewById<TextView>(R.id.txt_nombre_apellido).text = "${cliente.first_name} ${cliente.last_name}"
        findViewById<TextView>(R.id.txt_username).text = cliente.username
        findViewById<TextView>(R.id.txt_email).text = cliente.email
        findViewById<TextView>(R.id.txt_telefono).text = cliente.profile.telefono
        findViewById<TextView>(R.id.txt_address).text = cliente.profile.direccion
        // Puedes agregar más TextViews si necesitas mostrar más información
    }
}