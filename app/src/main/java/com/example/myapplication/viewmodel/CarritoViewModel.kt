package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.DetallePedido
import com.example.myapplication.service.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class CarritoViewModel(application: Application) : AndroidViewModel(application) {
    private val _carrito = MutableLiveData<MutableList<DetallePedido>>(mutableListOf())
    val carrito: LiveData<MutableList<DetallePedido>> get() = _carrito
    private val platoNombres = mutableListOf<Pair<Int, String>>()
    private val sharedPreferences = application.getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE)

    private val _precioTotal = MutableLiveData(0.0)
    val precioTotal: LiveData<Double> get() = _precioTotal

    init {
        cargarCarrito()
    }

    fun actualizarPrecioTotal() {
        val total = _carrito.value?.sumOf { it.precio_total } ?: 0.0
        _precioTotal.value = total
    }

    fun agregarAlCarrito(detalle: DetallePedido, nombrePlato: String) {
        platoNombres.add(Pair(detalle.plato, nombrePlato))
        val listaActualizada = _carrito.value?.toMutableList() ?: mutableListOf()
        listaActualizada.add(detalle)
        _carrito.value = listaActualizada
        guardarCarrito()
        viewModelScope.launch {
            actualizarPrecioTotalAsync()
        }
    }

    private suspend fun actualizarPrecioTotalAsync() {
        withContext(Dispatchers.Main) {
            val total = _carrito.value?.sumOf { it.precio_total } ?: 0.0
            _precioTotal.value = total
        }
    }

    fun actualizarCantidad(position: Int, cantidad: Int) {
        val listaActualizada = _carrito.value?.toMutableList() ?: return
        if (position in 0 until listaActualizada.size) {
            val detalle = listaActualizada[position]
            val precioUnitario = obtenerPrecioUnitario(detalle.plato)

            if (precioUnitario != null) {
                detalle.cantidad = cantidad
                detalle.precio_total = precioUnitario * cantidad
                _carrito.value = listaActualizada
                guardarCarrito()
                actualizarPrecioTotal()
            } else {
                Log.e("CarritoViewModel", "Precio unitario no encontrado para el plato ${detalle.plato}")
            }
        }
    }

    fun obtenerNombresPlatos(): List<Pair<Int, String>> {
        return platoNombres
    }

    private fun obtenerPrecioUnitario(platoId: Int): Double? = runBlocking {
        try {
            val plato = RetrofitInstance.getRetrofitAuth(getApplication()).getPlatoById(platoId)
            plato.precio
        } catch (e: Exception) {
            Log.e("CarritoViewModel", "Error al obtener el precio unitario", e)
            null
        }
    }

    fun vaciarCarrito() {
        _carrito.value?.clear()
        _carrito.value = mutableListOf()
        platoNombres.clear()
        guardarCarrito()
        actualizarPrecioTotal()
    }

    private fun guardarCarrito() {
        val carritoJson = Gson().toJson(_carrito.value)
        sharedPreferences.edit().putString("carrito", carritoJson).apply()
    }

    private fun cargarCarrito() {
        viewModelScope.launch {
            val carritoJson = withContext(Dispatchers.IO) {
                sharedPreferences.getString("carrito", null)
            }
            if (carritoJson != null) {
                val type = object : TypeToken<MutableList<DetallePedido>>() {}.type
                val carritoCargado: MutableList<DetallePedido> = Gson().fromJson(carritoJson, type)
                _carrito.value = carritoCargado
            }
            actualizarPrecioTotalAsync() // Actualizar el precio total después de cargar el carrito
        }
    }

    fun eliminarDelCarrito(detalle: DetallePedido) {
        val listaActualizada = _carrito.value?.toMutableList() ?: return
        listaActualizada.remove(detalle)
        _carrito.value = listaActualizada
        guardarCarrito()
        actualizarPrecioTotal()
    }
}