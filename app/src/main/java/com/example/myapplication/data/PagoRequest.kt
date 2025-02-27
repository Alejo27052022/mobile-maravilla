package com.example.myapplication.data

data class PagoRequest(
    val metodo_pago: String,
    val estado_pago: String,
    val pedido: Int
)