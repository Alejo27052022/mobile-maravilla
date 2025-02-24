package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.CategoriaPlatoAdapter
import com.example.myapplication.data.Datos
import com.example.myapplication.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PantallaCategoria : ComponentActivity() {
    private lateinit var platosAdapter: CategoriaPlatoAdapter  // Se usa el nuevo adaptador
    private val platos = mutableListOf<Datos>()
    private val retrofitService = RetrofitInstance.getRetrofitNoAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pantalla_categoria)

        val iconhome : ImageView = findViewById(R.id.icon_home)
        iconhome.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }

        val iconImage: ImageView = findViewById(R.id.icon_profile)
        iconImage.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val categoriaId = intent.getIntExtra("CATEGORIA_ID", -1)
        val categoriaNombre = intent.getStringExtra("CATEGORIA_NOMBRE")

        findViewById<TextView>(R.id.name_categoria).text = categoriaNombre

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_categoria_productos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Aquí inicializamos `CategoriaPlatoAdapter` en lugar de `MenuPlatosAdapter`
        platosAdapter = CategoriaPlatoAdapter(platos)
        recyclerView.adapter = platosAdapter

        if (categoriaId != -1) {
            fetchPlatosPorCategoria(categoriaId)
        }
    }

    private fun fetchPlatosPorCategoria(categoriaId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitService.getPlatos()
                val platosFiltrados = response.filter { it.categoria.id == categoriaId }

                withContext(Dispatchers.Main) {
                    platos.clear()
                    platos.addAll(platosFiltrados)
                    platosAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("PantallaCategoria", "Error al cargar los platos", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PantallaCategoria, "Error al cargar los platos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
