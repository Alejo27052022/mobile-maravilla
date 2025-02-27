package com.example.myapplication.data

data class DetallePedido(
    var cantidad: Int,
    var precio_total: Double,
    val pedido: Int,
    val plato: Int
)