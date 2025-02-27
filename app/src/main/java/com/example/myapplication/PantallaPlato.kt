package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.PlatoAdapter
import com.example.myapplication.data.Datos
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.service.RetrofitService
import com.example.myapplication.viewmodel.CarritoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PantallaPlato : ComponentActivity() {
    private lateinit var carritoViewModel: CarritoViewModel

    private lateinit var platoAdapter: PlatoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var retrofitService: RetrofitService
    private lateinit var editTextCantidad: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_plato)

        // Inicializa el ViewModel
        carritoViewModel = ViewModelProvider(this).get(CarritoViewModel::class.java)
        Log.d("PantallaPlato", "CarritoViewModel inicializado: $carritoViewModel")

        editTextCantidad = findViewById(R.id.txt_cantidad)
        retrofitService = RetrofitInstance.getRetrofitAuth(this)

        val iconCarrito: ImageView = findViewById(R.id.icon_buy)
        iconCarrito.setOnClickListener {
            Log.d("PantallaPlato", "Icono de carrito clickeado")
            val intent = Intent(this, com.example.myapplication.DetallePedido::class.java)
            startActivity(intent)
        }

        val iconImage: ImageView = findViewById(R.id.icon_profile)
        iconImage.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val iconhome : ImageView = findViewById(R.id.icon_home)
        iconhome.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recycler_view_plato)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val platoId = intent.getIntExtra("PLATO_ID", -1)

        if (platoId != -1) {
            fetchPlato(platoId)
        } else {
            Toast.makeText(this, "Error: No se encontró el plato", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPlato(platoId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val plato = retrofitService.getPlatoById(platoId)
                withContext(Dispatchers.Main) {
                    platoAdapter = PlatoAdapter(listOf(plato)) { selectedPlato ->
                        // Agregar el plato al carrito con cantidad predeterminada de 1
                        val detallePedido = DetallePedido(
                            cantidad = 1, // Cantidad predeterminada
                            precio_total = selectedPlato.precio, // Precio total para 1 plato
                            pedido = 1,
                            plato = selectedPlato.id
                        )

                        // Aquí pasamos el nombre del plato junto con el detalle
                        carritoViewModel.agregarAlCarrito(detallePedido, selectedPlato.name)
                        carritoViewModel.actualizarPrecioTotal() // Agregar esta línea
                        Log.d("PantallaPlato", "Plato agregado al carrito: $detallePedido")
                        Toast.makeText(this@PantallaPlato, "${selectedPlato.name} agregado", Toast.LENGTH_SHORT).show()
                    }
                    recyclerView.adapter = platoAdapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PantallaPlato", "Error al cargar el plato", e)
                    Toast.makeText(this@PantallaPlato, "Error al cargar el plato", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

