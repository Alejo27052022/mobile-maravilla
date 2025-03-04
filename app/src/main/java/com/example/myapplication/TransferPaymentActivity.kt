package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.Mesa
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.data.MisPedidos
import com.example.myapplication.data.Pago
import com.example.myapplication.data.PagoRequest
import com.example.myapplication.data.PedidoRequest
import com.example.myapplication.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.RequestBody.Companion.toRequestBody

class TransferPaymentActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var mesaSeleccionada: Int? = null
    private lateinit var carrito: MutableList<DetallePedido>
    private lateinit var sendButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_pago_transf)

        carrito = getCarritoFromSharedPreferences()

        val uploadButton = findViewById<Button>(R.id.upload_image_button)
        sendButton = findViewById(R.id.send_payment_button)
        val imageView = findViewById<ImageView>(R.id.image_view)

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
        mesaSeleccionada = sharedPreferences.getInt("mesaSeleccionada", 0) // Recupera el número de mesa como Int

        Log.d("onCreate", "mesaSeleccionada: $mesaSeleccionada")

        sendButton.setOnClickListener {
            sendButton.isEnabled = false
            Log.d("TransferPaymentActivity", "mesaSeleccionada: $mesaSeleccionada")
            Log.d("TransferPaymentActivity", "carrito: $carrito")
            enviarPedido()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            val imageView = findViewById<ImageView>(R.id.image_view)
            imageView.setImageURI(imageUri)
        }
    }

    private fun enviarPedido() {
        if (mesaSeleccionada == null || carrito.isNullOrEmpty()) {
            Toast.makeText(this, "Datos del pedido incompletos", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val pedidoRequest = PedidoRequest(mesa = mesaSeleccionada!!)
                val response = RetrofitInstance.getRetrofitAuth(this@TransferPaymentActivity).crearPedido(pedidoRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        RetrofitInstance.getRetrofitAuth(this@TransferPaymentActivity).obtenerMisPedidos().enqueue(object : retrofit2.Callback<List<MisPedidos>> {
                            override fun onResponse(call: retrofit2.Call<List<MisPedidos>>, response: retrofit2.Response<List<MisPedidos>>) {
                                if (response.isSuccessful) {
                                    val pedidos = response.body() ?: emptyList()
                                    val pedidoReciente = pedidos.lastOrNull { pedido: MisPedidos -> pedido.mesa == mesaSeleccionada }

                                    val pedidoId = pedidoReciente?.id ?: run {
                                        Log.e("TransferPaymentActivity", "Error al obtener pedidoId de mis-pedidos")
                                        Toast.makeText(this@TransferPaymentActivity, "Error al obtener el ID del pedido", Toast.LENGTH_SHORT).show()
                                        sendButton.isEnabled = true
                                        return
                                    }

                                    carrito.forEach { detalle ->
                                        RetrofitInstance.getRetrofitAuth(this@TransferPaymentActivity).agregarDetallePedido(DetallePedido(cantidad = detalle.cantidad, precio_total = detalle.precio_total, pedido = pedidoId, plato = detalle.plato)).enqueue(object : retrofit2.Callback<DetallePedido> {
                                            override fun onResponse(call: retrofit2.Call<DetallePedido>, response: retrofit2.Response<DetallePedido>) {
                                                if (!response.isSuccessful) {
                                                    Log.e("TransferPaymentActivity", "Error al agregar detalle: ${response.errorBody()?.string()}")
                                                    Toast.makeText(this@TransferPaymentActivity, "Error al agregar detalle", Toast.LENGTH_SHORT).show()
                                                    sendButton.isEnabled = true
                                                    return
                                                }
                                            }

                                            override fun onFailure(call: retrofit2.Call<DetallePedido>, t: Throwable) {
                                                Log.e("TransferPaymentActivity", "Error al agregar detalle: ${t.message}", t)
                                                Toast.makeText(this@TransferPaymentActivity, "Error al agregar detalle: ${t.message}", Toast.LENGTH_SHORT).show()
                                                sendButton.isEnabled = true
                                                return
                                            }
                                        })
                                    }

                                    val pagoRequest = PagoRequest(metodo_pago = "transferencia", estado_pago = "pendiente", pedido = pedidoId)
                                    val metodoPago = pagoRequest.metodo_pago?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val estadoPago = pagoRequest.estado_pago?.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val pedido = pagoRequest.pedido?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                                    val imageFile = imageUri?.let { uriToFile(it) }
                                    val requestFile = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())
                                    val comprobantePago = requestFile?.let { MultipartBody.Part.createFormData("comprobante_pago", imageFile.name, it) }

                                    RetrofitInstance.getRetrofitAuth(this@TransferPaymentActivity).enviarPago(metodoPago!!, estadoPago!!, pedido!!, comprobantePago).enqueue(object : retrofit2.Callback<Pago> {
                                        override fun onResponse(call: retrofit2.Call<Pago>, response: retrofit2.Response<Pago>) {
                                            if (response.isSuccessful) {
                                                Toast.makeText(this@TransferPaymentActivity, "Pedido confirmado", Toast.LENGTH_SHORT).show()
                                                val builder = AlertDialog.Builder(this@TransferPaymentActivity)
                                                val inflater = layoutInflater
                                                val dialogView = inflater.inflate(R.layout.pantalla_confirmacion_pago, null)
                                                builder.setView(dialogView)
                                                builder.setPositiveButton("Aceptar") { dialog, _ ->
                                                    dialog.dismiss()
                                                    limpiarCarrito()
                                                    val intent = Intent(this@TransferPaymentActivity, Inicio::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                }

                                                val dialog = builder.create()
                                                dialog.show()
                                            } else {
                                                Toast.makeText(this@TransferPaymentActivity, "Error al enviar pago", Toast.LENGTH_SHORT).show()
                                                sendButton.isEnabled = true
                                            }
                                        }

                                        override fun onFailure(call: retrofit2.Call<Pago>, t: Throwable) {
                                            Toast.makeText(this@TransferPaymentActivity, "Error al enviar pago: ${t.message}", Toast.LENGTH_SHORT).show()
                                            sendButton.isEnabled = true
                                        }
                                    })
                                } else {
                                    Toast.makeText(this@TransferPaymentActivity, "Error al obtener mis pedidos", Toast.LENGTH_SHORT).show()
                                    sendButton.isEnabled = true
                                }
                            }

                            override fun onFailure(call: retrofit2.Call<List<MisPedidos>>, t: Throwable) {
                                Toast.makeText(this@TransferPaymentActivity, "Error al obtener mis pedidos: ${t.message}", Toast.LENGTH_SHORT).show()
                                sendButton.isEnabled = true
                            }
                        })
                    } else {
                        Toast.makeText(this@TransferPaymentActivity, "Error al crear el pedido: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                        sendButton.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TransferPaymentActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    sendButton.isEnabled = true
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

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File.createTempFile("image", null, cacheDir)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        return file
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

    private fun getMesaFromSharedPreferences(): Mesa? {
        val sharedPreferences = getSharedPreferences("PedidoPrefs", Context.MODE_PRIVATE)
        val mesaJson = sharedPreferences.getString("mesaSeleccionada", null)
        Log.d("getMesaFromSharedPreferences", "mesaJson: $mesaJson") // Imprimir mesaJson

        if (mesaJson != null) {
            val gson = Gson()
            val mesa = gson.fromJson(mesaJson, Mesa::class.java)
            Log.d("getMesaFromSharedPreferences", "mesa: $mesa") // Imprimir objeto Mesa
            return mesa
        }
        return null
    }
}