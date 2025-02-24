package com.example.myapplication.data
import com.google.gson.annotations.SerializedName

data class Datos(
    val id: Int,
    @SerializedName("nombre_plato") val name: String,
    val descripcion: String,
    @SerializedName("img_plato") val imgPlato: String,
    val precio: String,
    val estado: String,
    val tiempo: Int,
    val categoria: Categoria
)
