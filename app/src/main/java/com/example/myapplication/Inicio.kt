package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.Datos
import com.example.myapplication.service.RetrofitInstance
import com.example.myapplication.service.RetrofitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Inicio : ComponentActivity() {

    private lateinit var retrofitService: RetrofitService

    private lateinit var recyclerView: RecyclerView
    private val platos = mutableListOf<Datos>()
    private lateinit var platosAdapter: MenuPlatosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_principal)

        retrofitService = RetrofitInstance.getRetrofitService(this)

        /* Configuracion de RecyclerView */
        recyclerView = findViewById(R.id.recycler_view)
        platosAdapter = MenuPlatosAdapter(platos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = platosAdapter

        // Llamada de API
        fetchPlatos()
    }

    private fun fetchPlatos(){
        // Corrutina
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = retrofitService.getPlatos()
                withContext(Dispatchers.Main){
                    platos.clear()
                    platos.addAll(response)
                    platosAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    Toast.makeText(
                        this@Inicio,
                        "Error al cargar los datos: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}