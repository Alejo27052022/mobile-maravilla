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
import com.example.myapplication.data.MisPedidos
import com.example.myapplication.data.PedidoRequest
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.viewmodel.CarritoViewModel
import com.example.myapplication.viewmodel.MesaViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DetallePedido : AppCompatActivity() {
    private lateinit var carritoViewModel: CarritoViewModel
    private lateinit var mesaViewModel: MesaViewModel
    private lateinit var adapter: CarritoAdapter
    private lateinit var mesaAdapter: MesaAdapter
    private var mesaSeleccionada: Mesa? = null
    private lateinit var txtCantidad: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_pedido)

        txtCantidad = findViewById(R.id.txt_cantidad)
        carritoViewModel = ViewModelProvider(this).get(CarritoViewModel::class.java)
        mesaViewModel = ViewModelProvider(this).get(MesaViewModel::class.java)

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

        mesaAdapter = MesaAdapter(emptyList()) { mesa ->
            mesaSeleccionada = mesa
            Toast.makeText(this, "Mesa seleccionada: ${mesa.numero_mesa}", Toast.LENGTH_SHORT).show()
        }
        recyclerViewMesas.adapter = mesaAdapter

        mesaViewModel.mesas.observe(this) { mesas ->
            mesaAdapter = MesaAdapter(mesas) { mesa ->
                mesaSeleccionada = mesa
                Toast.makeText(this, "Mesa seleccionada: ${mesa.numero_mesa}", Toast.LENGTH_SHORT).show()
            }
            recyclerViewMesas.adapter = mesaAdapter
        }

        mesaViewModel.obtenerMesas()

        findViewById<Button>(R.id.btn_pagar).setOnClickListener {
            if (mesaSeleccionada == null) {
                Toast.makeText(this, "Por favor, selecciona una mesa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gson = Gson() // Inicializa Gson
            val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val numeroMesa = mesaSeleccionada!!.numero_mesa // Obtén el número de mesa
            Log.d("DetallePedido", "Mesa a guardar: $numeroMesa") // Imprimir numeroMesa
            editor.putInt("mesaSeleccionada", numeroMesa) // Guarda el número de mesa como Int
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

    private suspend fun enviarPedido() {
        val mesaNumero = mesaSeleccionada?.numero_mesa ?: run {
            Toast.makeText(this, "Por favor, selecciona una mesa", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val pedidoRequest = PedidoRequest(
                    mesa = mesaNumero
                )

                val response = RetrofitInstance.getRetrofitAuth(this@DetallePedido).crearPedido(pedidoRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        RetrofitInstance.getRetrofitAuth(this@DetallePedido).obtenerMisPedidos().enqueue(object : retrofit2.Callback<List<MisPedidos>> {
                            override fun onResponse(call: retrofit2.Call<List<MisPedidos>>, response: retrofit2.Response<List<MisPedidos>>) {
                                if (response.isSuccessful) {
                                    val pedidos = response.body() ?: emptyList()
                                    val pedidoReciente = pedidos.lastOrNull { pedido: MisPedidos -> pedido.mesa == mesaNumero }

                                    val pedidoId = pedidoReciente?.id ?: run {
                                        Log.e("DetallePedido", "Error al obtener pedidoId de mis-pedidos")
                                        Toast.makeText(this@DetallePedido, "Error al obtener el ID del pedido", Toast.LENGTH_SHORT).show()
                                        return
                                    }

                                    Log.d("DetallePedido", "ID del pedido obtenido: $pedidoId") // Log adicional

                                    carritoViewModel.carrito.value?.forEach { detalle ->
                                        val cantidad = txtCantidad.text.toString().toIntOrNull() ?: 1
                                        val detallePedido = DetallePedido(
                                            cantidad = cantidad,
                                            precio_total = detalle.precio_total,
                                            pedido = pedidoId,
                                            plato = detalle.plato
                                        )

                                        Log.d("DetallePedido", "DetallePedido a enviar: $detallePedido") // Log adicional

                                        // Log justo antes de la llamada a agregarDetallePedido()
                                        Log.d("DetallePedido", "Llamando a agregarDetallePedido con: $detallePedido")

                                        RetrofitInstance.getRetrofitAuth(this@DetallePedido).agregarDetallePedido(detallePedido).enqueue(object : retrofit2.Callback<DetallePedido> {
                                            override fun onResponse(call: retrofit2.Call<DetallePedido>, response: retrofit2.Response<DetallePedido>) {
                                                Log.d("DetallePedido", "Respuesta de agregarDetallePedido: ${response.code()}")
                                                if (response.isSuccessful) {
                                                    Log.d("DetallePedido", "DetallePedido enviado correctamente: ${response.body()}")
                                                } else {
                                                    Log.e("DetallePedido", "Error al agregar detalle: ${response.errorBody()?.string()}")
                                                    Toast.makeText(this@DetallePedido, "Error al agregar detalle", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            override fun onFailure(call: retrofit2.Call<DetallePedido>, t: Throwable) {
                                                Log.e("DetallePedido", "Error al agregar detalle: ${t.message}", t)
                                                Toast.makeText(this@DetallePedido, "Error al agregar detalle: ${t.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }

                                    Toast.makeText(this@DetallePedido, "Pedido confirmado", Toast.LENGTH_SHORT).show()
                                    carritoViewModel.vaciarCarrito()
                                    finish()
                                } else {
                                    Log.e("DetallePedido", "Error al obtener mis pedidos: ${response.errorBody()?.string()}")
                                    Toast.makeText(this@DetallePedido, "Error al obtener mis pedidos", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<List<MisPedidos>>, t: Throwable) {
                                Log.e("DetallePedido", "Error al obtener mis pedidos: ${t.message}", t)
                                Toast.makeText(this@DetallePedido, "Error al obtener mis pedidos: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@DetallePedido, "Error al crear el pedido: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DetallePedido", "Error de conexión general: ${e.message}", e)
                    Toast.makeText(this@DetallePedido, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
