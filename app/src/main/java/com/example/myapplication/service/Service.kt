package com.example.myapplication.service

import android.content.Context
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Clientes
import retrofit2.http.GET
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import com.google.gson.annotations.SerializedName
import com.example.myapplication.data.Datos
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

interface RetrofitService{
    /* Métodos GET*/
    @GET("platos/")
    suspend fun getPlatos(): List<Datos>

    @GET("categorias/")
    suspend fun getCategorias(): List<Categoria>

    @POST("register/")
    fun registerUser(@Body request: Clientes): Call<ResponseBody>

    @POST("login/")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "https://melody19.pythonanywhere.com/api/"

    // Retrofit SIN autenticación (para registro e inicio de sesión)
    fun getRetrofitNoAuth(): RetrofitService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(RetrofitService::class.java)
    }

    // Retrofit CON autenticación (para endpoints protegidos)
    fun getRetrofitAuth(context: Context): RetrofitService {
        val sharedPreferences = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        val client = OkHttpClient.Builder().apply {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            if (!token.isNullOrEmpty()) {
                addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Token $token")
                        .build()
                    chain.proceed(request)
                }
            }
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(RetrofitService::class.java)
    }
}



