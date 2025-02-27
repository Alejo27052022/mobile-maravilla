package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PaymentMethodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_pago)

        val transferLayout = findViewById<LinearLayout>(R.id.linear_layout_transf)
        val cashLayout = findViewById<LinearLayout>(R.id.linear_layout_efectivo)

        transferLayout.setOnClickListener {
            val intent = Intent(this, TransferPaymentActivity::class.java)
            // Pasar datos del pedido a TransferPaymentActivity
            // intent.putExtra("mesaSeleccionada", mesaSeleccionada)
            startActivity(intent)
        }

        cashLayout.setOnClickListener {
            // Implementar la lógica para el pago en efectivo
            // (puedes iniciar otra actividad o mostrar un diálogo)
        }
    }
}