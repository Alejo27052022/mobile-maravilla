package com.example.myapplication.data

data class Pago(
    val id: Int?, // Agrega el campo id
    val metodo_pago: String?,
    val estado_pago: String?,
    val comprobante_pago: String?,
    val pedido: Int? // O el tipo de dato que represente el ID del pedido
)