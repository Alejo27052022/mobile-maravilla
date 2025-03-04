package com.example.myapplication.data
import java.io.Serializable

data class Mesa(
    val id: Int? = null,
    val numero_mesa: Int,
    val estado_mesa: String
) : Serializable