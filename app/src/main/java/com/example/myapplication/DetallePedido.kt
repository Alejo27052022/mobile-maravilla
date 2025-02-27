package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.CarritoAdapter
import com.example.myapplication.adaptadores.MesaAdapter
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.data.Mesa
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.viewmodel.CarritoViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetallePedido : AppCompatActivity() {
    private lateinit var carritoViewModel: CarritoViewModel
    private lateinit var adapter: CarritoAdapter
    private lateinit var mesaAdapter: MesaAdapter
    private var mesaSeleccionada: Int? = null
    private lateinit var txtCantidad: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_pedido)

        txtCantidad = findViewById(R.id.txt_cantidad)
        carritoViewModel = ViewModelProvider(this).get(CarritoViewModel::class.java)

        adapter = CarritoAdapter(mutableListOf(), carritoViewModel, RetrofitInstance.getRetrofitAuth(this@DetallePedido), this)
        findViewById<RecyclerView>(R.id.recyclerviewPlatos).apply {
            layoutManager = LinearLayoutManager(this@DetallePedido)
            adapter = this@DetallePedido.adapter
        }

        carritoViewModel.carrito.observe(this) { lista ->
            adapter.actualizarLista(lista.toMutableList())
            Log.d("DetallePedido", "Lista actualizada: $lista")
        }

        carritoViewModel.precioTotal.observe(this) { total ->
            findViewById<TextView>(R.id.txt_total_pagar).text = "$${String.format("%.2f", total)}"
        }

        val recyclerViewMesas: RecyclerView = findViewById(R.id.recyclerViewMesas)
        recyclerViewMesas.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val mesasDisponibles = (1..10).toList()
        mesaAdapter = MesaAdapter(mesasDisponibles) { mesa ->
            mesaSeleccionada = mesa
            Toast.makeText(this, "Mesa seleccionada: $mesa", Toast.LENGTH_SHORT).show()
        }
        recyclerViewMesas.adapter = mesaAdapter

        findViewById<Button>(R.id.btn_pagar).setOnClickListener {
            if (mesaSeleccionada == null) {
                Toast.makeText(this, "Por favor, selecciona una mesa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("mesaSeleccionada", mesaSeleccionada!!)
            val gson = Gson()
            val carritoJson = gson.toJson(carritoViewModel.carrito.value)
            editor.putString("carrito", carritoJson)
            editor.apply()

            val intent = Intent(this, PaymentMethodActivity::class.java)
            startActivity(intent)
        }

        val iconImage: ImageView = findViewById(R.id.icon_profile)
        iconImage.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val iconhome: ImageView = findViewById(R.id.icon_home)
        iconhome.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }
    }

    private fun enviarPedido() {
        val mesaNumero = mesaSeleccionada ?: run {
            Toast.makeText(this, "Por favor, selecciona una mesa", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.getRetrofitAuth(this@DetallePedido).crearPedido(Mesa(mesa = mesaNumero)).enqueue(object : Callback<Mesa> {
                    override fun onResponse(call: Call<Mesa>, response: Response<Mesa>) {
                        if (response.isSuccessful) {
                            val pedidoId = response.body()?.id ?: return

                            carritoViewModel.carrito.value?.forEach { detalle ->
                                val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 1
                                val detallePedido = DetallePedido(
                                    cantidad = cantidad,
                                    precio_total = detalle.precio_total,
                                    pedido = pedidoId,
                                    plato = detalle.plato
                                )

                                RetrofitInstance.getRetrofitAuth(this@DetallePedido).agregarDetallePedido(detallePedido).enqueue(object : Callback<DetallePedido> {
                                    override fun onResponse(call: Call<DetallePedido>, response: Response<DetallePedido>) {
                                        if (!response.isSuccessful) {
                                            lifecycleScope.launch(Dispatchers.Main) {
                                                Toast.makeText(this@DetallePedido, "Error al agregar detalle", Toast.LENGTH_SHORT).show()
                                            }
                                            return
                                        }
                                    }

                                    override fun onFailure(call: Call<DetallePedido>, t: Throwable) {
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            Toast.makeText(this@DetallePedido, "Error al agregar detalle: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        return
                                    }
                                })
                            }

                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(this@DetallePedido, "Pedido confirmado", Toast.LENGTH_SHORT).show()
                                carritoViewModel.vaciarCarrito()
                                finish()
                            }
                        } else {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(this@DetallePedido, "Error al crear el pedido", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Mesa>, t: Throwable) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@DetallePedido, "Error al crear el pedido: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DetallePedido, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}