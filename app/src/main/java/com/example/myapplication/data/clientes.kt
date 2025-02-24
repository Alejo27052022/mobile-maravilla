package com.example.myapplication.data

import com.google.gson.annotations.SerializedName

data class Profile(
    val telefono: String,
    val direccion: String,
    val cedula: String
)


data class Clientes(
    val username: String,
    val email: String,
    val password: String,
    val first_name: String,
    val last_name: String,
    val profile: Profile
)