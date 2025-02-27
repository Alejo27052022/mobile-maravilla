package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adaptadores.CategoriaAdapter
import com.example.myapplication.adaptadores.MenuPlatosAdapter
import com.example.myapplication.adaptadores.OnCategoriaClickListener
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

        // Verificar si el token está presente
        val sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)
        Log.d("Inicio", "Token almacenado: $token")

        // Si no hay token, redirigir al Login
        if (token.isNullOrEmpty()) {
            Log.d("Inicio", "No hay token, redirigiendo al Login")
            startActivity(Intent(this, Login::class.java))
            finish() // Evitar que el usuario regrese a esta pantalla con el botón "Atrás"
            return
        }

        // Si el token está presente, continuar con la carga de la actividad
        retrofitService = RetrofitInstance.getRetrofitAuth(this)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerViewCategoria = findViewById(R.id.recycler_view_categoria)

        val nombreUsuario = sharedPreferences.getString("NOMBRE_USUARIO", "Usuario Default") // Valor predeterminado
        val textViewNombre: TextView = findViewById(R.id.nombre_usuario)
        textViewNombre.text = "Hola $nombreUsuario"

        // Configurar el adaptador de platos con el clic
        platosAdapter = MenuPlatosAdapter(platos) { plato ->
            val intent = Intent(this, PantallaPlato::class.java)
            intent.putExtra("PLATO_ID", plato.id) // Pasamos el ID del plato
            startActivity(intent)
        }

        categoriaAdapter = CategoriaAdapter(categoria, object : OnCategoriaClickListener {
            override fun onCategoriaClick(categoria: Categoria) {
                val intent = Intent(this@Inicio, PantallaCategoria::class.java)
                intent.putExtra("CATEGORIA_ID", categoria.id)
                intent.putExtra("CATEGORIA_NOMBRE", categoria.nombre)
                startActivity(intent)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = platosAdapter
        recyclerViewCategoria.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCategoria.adapter = categoriaAdapter

        val iconImage: ImageView = findViewById(R.id.icon_profile)
        iconImage.setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val iconhome : ImageView = findViewById(R.id.icon_home)
        iconhome.setOnClickListener {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
        }

        val iconCarrito: ImageView = findViewById(R.id.icon_buy)
        iconCarrito.setOnClickListener {
            Log.d("PantallaPlato", "Icono de carrito clickeado")
            val intent = Intent(this, com.example.myapplication.DetallePedido::class.java)
            startActivity(intent)
        }

        iconhome.isSelected = true

        // Llamada a la API para obtener la información del usuario
        fetchUserInfo()
        // Llamada a la API para obtener platos y categorías
        fetchAPI()
    }

    // Función para obtener los detalles del usuario
    private fun fetchUserInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Hacer la solicitud al endpoint para obtener los datos del usuario
                val userProfile = retrofitService.getUserProfile()
                withContext(Dispatchers.Main) {
                    // Mostrar el nombre completo del usuario en la UI
                    val nombreCompleto = "${userProfile.first_name} ${userProfile.last_name}"
                    val textViewNombre: TextView = findViewById(R.id.nombre_usuario)
                    textViewNombre.text = "Hola $nombreCompleto"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Inicio,
                        "Error al cargar la información del usuario: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Función para cargar los platos y categorías (ya implementada)
    private fun fetchAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitService.getPlatos()
                val response2 = retrofitService.getCategorias()
                withContext(Dispatchers.Main) {
                    platos.clear()
                    categoria.clear()
                    platos.addAll(response)
                    categoria.addAll(response2)
                    platosAdapter.notifyDataSetChanged()
                    categoriaAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
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


