package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.CategoriaAdapter
import com.example.myapplication.adaptadores.MenuPlatosAdapter
import com.example.myapplication.data.Categoria
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
    private lateinit var recyclerViewCategoria: RecyclerView
    private val platos = mutableListOf<Datos>()
    private val categoria = mutableListOf<Categoria>()
    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var platosAdapter: MenuPlatosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_principal)

        retrofitService = RetrofitInstance.getRetrofitService(this)

        /* Configuracion de Icono Home */
        val iconhome : ImageView = findViewById(R.id.icon_home)
        iconhome.isSelected = true

        /* Configuracion de RecyclerView */
        recyclerView = findViewById(R.id.recycler_view)
        recyclerViewCategoria = findViewById(R.id.recycler_view_categoria)

        platosAdapter = MenuPlatosAdapter(platos)
        categoriaAdapter = CategoriaAdapter(categoria)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = platosAdapter
        recyclerViewCategoria.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCategoria.adapter = categoriaAdapter

        // Llamada de API
        fetchAPI()
    }

    private fun fetchAPI(){
        // Corrutina
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val response = retrofitService.getPlatos()
                val response2 = retrofitService.getCategorias()
                withContext(Dispatchers.Main){
                    platos.clear()
                    categoria.clear()
                    platos.addAll(response)
                    categoria.addAll(response2)
                    platosAdapter.notifyDataSetChanged()
                    categoriaAdapter.notifyDataSetChanged()
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