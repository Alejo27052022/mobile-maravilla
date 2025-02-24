package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.myapplication.data.Clientes
import com.example.myapplication.data.Profile
import com.example.myapplication.service.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val cedula_user: EditText = findViewById(R.id.edit_cedula)
        val nombre: EditText = findViewById(R.id.edit_name)
        val apellido: EditText = findViewById(R.id.edit_last)
        val usuario: EditText = findViewById(R.id.input_usuario)
        val email: EditText = findViewById(R.id.edit_email)
        val password: EditText = findViewById(R.id.edit_pass)
        val telefono_user: EditText = findViewById(R.id.edit_telefono)
        val direccion_user: EditText = findViewById(R.id.edit_address)
        val registrar: Button = findViewById(R.id.btn_enviar_registro)

        val btn_volver : Button = findViewById(R.id.btn_volver_login)
        btn_volver.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        registrar.setOnClickListener {
            val cedula = cedula_user.text.toString().trim()
            val name = nombre.text.toString().trim()
            val lastname = apellido.text.toString().trim()
            val user = usuario.text.toString().trim()
            val correo = email.text.toString().trim()
            val contra = password.text.toString().trim()
            val telefono = telefono_user.text.toString().trim()
            val direccion = direccion_user.text.toString().trim()

            if (name.isNotEmpty() && lastname.isNotEmpty() && user.isNotEmpty() &&
                correo.isNotEmpty() && contra.isNotEmpty() && telefono.isNotEmpty() && direccion.isNotEmpty()
                && cedula.isNotEmpty()){
                registerUser(name, lastname, user, correo, contra, telefono, direccion, cedula)
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(
        name: String, lastname: String, user: String, correo: String,
        contra: String, telefono: String, direccion: String, cedula: String
    ) {
        val request = Clientes(
            username = user,
            email = correo,
            password = contra,
            first_name = name,
            last_name = lastname,
            profile = Profile(telefono, direccion, cedula)
        )

        val call = RetrofitInstance.getRetrofitNoAuth().registerUser(request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Register, "Registro exitoso", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@Register, Login::class.java))
                    finish()
                } else {
                    // Captura la respuesta de error y la muestra en Logcat
                    val errorBody = response.errorBody()?.string()
                    Log.e("Retrofit", "Error en el registro: $errorBody")
                    Toast.makeText(this@Register, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("Retrofit", "Fallo en la solicitud: ${t.message}")
                Toast.makeText(this@Register, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}