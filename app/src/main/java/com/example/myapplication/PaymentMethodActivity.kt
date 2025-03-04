package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.data.Mesa
import com.example.myapplication.data.MisPedidos
import com.example.myapplication.data.Pago
import com.example.myapplication.data.PagoRequest
import com.example.myapplication.data.PedidoRequest
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.viewmodel.CarritoViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class PaymentMethodActivity : AppCompatActivity() {

    private lateinit var carritoViewModel: CarritoViewModel
    private var mesaSeleccionada: Int? = null
    private lateinit var carrito: MutableList<DetallePedido>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_pago)

        carritoViewModel = ViewModelProvider(this)[CarritoViewModel::class.java]
        carrito = getCarritoFromSharedPreferences()

        val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
        mesaSeleccionada = sharedPreferences.getInt("mesaSeleccionada", 0) // Línea 44: Recupera como Int

        val transferLayout = findViewById<LinearLayout>(R.id.linear_layout_transf)
        val cashLayout = findViewById<LinearLayout>(R.id.linear_layout_efectivo)

        transferLayout.setOnClickListener {
            val intent = Intent(this, TransferPaymentActivity::class.java)
            startActivity(intent)
        }

        cashLayout.setOnClickListener {
            enviarPedidoEfectivo()
        }
    }

    private fun enviarPedidoEfectivo() {
        if (mesaSeleccionada == null || carrito.isNullOrEmpty()) {
            Toast.makeText(this, "Datos del pedido incompletos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Crear el pedido
                val pedidoRequest = PedidoRequest(mesa = mesaSeleccionada!!)
                val response = RetrofitInstance.getRetrofitAuth(this@PaymentMethodActivity).crearPedido(pedidoRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // 2. Obtener el id del pedido desde mis-pedidos
                        RetrofitInstance.getRetrofitAuth(this@PaymentMethodActivity).obtenerMisPedidos().enqueue(object : retrofit2.Callback<List<MisPedidos>> {
                            override fun onResponse(call: retrofit2.Call<List<MisPedidos>>, response: retrofit2.Response<List<MisPedidos>>) {
                                if (response.isSuccessful) {
                                    val pedidos = response.body() ?: emptyList()
                                    val pedidoReciente = pedidos.lastOrNull { pedido: MisPedidos -> pedido.mesa == mesaSeleccionada }

                                    val pedidoId = pedidoReciente?.id ?: run {
                                        Log.e("PaymentMethodActivity", "Error al obtener pedidoId de mis-pedidos")
                                        Toast.makeText(this@PaymentMethodActivity, "Error al obtener el ID del pedido", Toast.LENGTH_SHORT).show()
                                        return
                                    }

                                    // 3. Crear los detallepedidos
                                    carrito.forEach { detalle ->
                                        RetrofitInstance.getRetrofitAuth(this@PaymentMethodActivity).agregarDetallePedido(DetallePedido(cantidad = detalle.cantidad, precio_total = detalle.precio_total, pedido = pedidoId, plato = detalle.plato)).enqueue(object : retrofit2.Callback<DetallePedido> {
                                            override fun onResponse(call: retrofit2.Call<DetallePedido>, response: retrofit2.Response<DetallePedido>) {
                                                if (!response.isSuccessful) {
                                                    Log.e("PaymentMethodActivity", "Error al agregar detalle: ${response.errorBody()?.string()}")
                                                    Toast.makeText(this@PaymentMethodActivity, "Error al agregar detalle", Toast.LENGTH_SHORT).show()
                                                    return
                                                }
                                            }

                                            override fun onFailure(call: retrofit2.Call<DetallePedido>, t: Throwable) {
                                                Log.e("PaymentMethodActivity", "Error al agregar detalle: ${t.message}", t)
                                                Toast.makeText(this@PaymentMethodActivity, "Error al agregar detalle: ${t.message}", Toast.LENGTH_SHORT).show()
                                                return
                                            }
                                        })
                                    }

                                    // 4. Enviar el pago (datos JSON) - EFECTIVO
                                    val pagoRequest = PagoRequest(metodo_pago = "efectivo", estado_pago = "pendiente", pedido = pedidoId)

                                    val metodoPago = pagoRequest.metodo_pago?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val estadoPago = pagoRequest.estado_pago?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val pedido = pagoRequest.pedido?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                                    RetrofitInstance.getRetrofitAuth(this@PaymentMethodActivity).enviarPago(metodoPago!!, estadoPago!!, pedido!!, null).enqueue(object : retrofit2.Callback<Pago> {
                                        override fun onResponse(call: retrofit2.Call<Pago>, response: retrofit2.Response<Pago>) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(this@PaymentMethodActivity, "Pedido confirmado", Toast.LENGTH_SHORT).show()

                                                // Crear y mostrar el AlertDialog
                                                val builder = AlertDialog.Builder(this@PaymentMethodActivity)
                                                val inflater = layoutInflater
                                                val dialogView = inflater.inflate(R.layout.pantalla_espera_pago, null)
                                                builder.setView(dialogView)

                                                // Modificar el botón "Aceptar" para redireccionar a la pantalla de inicio
                                                builder.setPositiveButton("Aceptar") { dialog, _ ->
                                                    dialog.dismiss()
                                                    // Limpiar el carrito
                                                    limpiarCarrito()
                                                    // Redireccionar a la pantalla de inicio
                                                    val intent = Intent(this@PaymentMethodActivity, Inicio::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                }

                                                val dialog = builder.create()
                                                dialog.show()
                                            } else {
                                                Toast.makeText(this@PaymentMethodActivity, "Error al enviar pago", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: retrofit2.Call<Pago>, t: Throwable) {
                                            Toast.makeText(this@PaymentMethodActivity, "Error al enviar pago: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                } else {
                                    Toast.makeText(this@PaymentMethodActivity, "Error al obtener mis pedidos", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<List<MisPedidos>>, t: Throwable) {
                                Toast.makeText(this@PaymentMethodActivity, "Error al obtener mis pedidos: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@PaymentMethodActivity, "Error al crear el pedido: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PaymentMethodActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun limpiarCarrito() {
        carrito.clear()
        val sharedPreferences: SharedPreferences = getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun getCarritoFromSharedPreferences(): MutableList<DetallePedido> {
        val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
        val carritoJson = sharedPreferences.getString("carrito", null)
        if (carritoJson != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<DetallePedido>>() {}.type
            return gson.fromJson(carritoJson, type)
        }
        return mutableListOf()
    }
}