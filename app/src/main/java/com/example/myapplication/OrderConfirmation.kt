package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity

class OrderConfirmation : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_confirmacion_pago)
    }
}