package com.example.myapplication.data
import com.google.gson.annotations.SerializedName

data class Categoria(
    val id: Int,
    val nombre: String,
    @SerializedName("img_categoria") val imgPlato: String
)