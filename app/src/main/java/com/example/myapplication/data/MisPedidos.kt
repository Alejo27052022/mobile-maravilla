package com.example.myapplication.data

data class MisPedidos(
    val id: Int,
    val usuario: Int,
    val fecha_entrega: String,
    val mesa: Int,
    val estado: String
)