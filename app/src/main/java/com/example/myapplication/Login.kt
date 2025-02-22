package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.service.LoginRequest
import com.example.myapplication.service.LoginResponse
import com.example.myapplication.service.RetrofitInstance
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val btn_register : Button = findViewById(R.id.btn_registrar)
        btn_register.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        val edit_user: EditText = findViewById(R.id.usernameInput)
        val edit_pass: EditText = findViewById(R.id.passwordInput)
        val btn_inicio: Button = findViewById(R.id.btn_iniciar_sesion)

        btn_inicio.setOnClickListener {
            val username = edit_user.text.toString().trim()
            val password = edit_pass.text.toString().trim()

            if(username.isNotEmpty() && password.isNotEmpty()){
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Por favor, ingresar usuario y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String){
        val request = LoginRequest(username, password)
        val call = RetrofitInstance.getRetrofitService(this).loginUser(request)

        call.enqueue(object: Callback<LoginResponse >{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if(response.isSuccessful){
                    val token = response.body()?.token

                    if (!token.isNullOrEmpty()){
                        saveToken(token)
                        Toast.makeText(this@Login, "Inicio de sesión exitoso", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@Login, Inicio::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@Login, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(p0: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@Login, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveToken(token: String){
        val sharedPreferences = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("TOKEN", token).apply()
    }
}