package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MisDetallePedidos
import com.example.myapplication.data.MisPedidos
import com.example.myapplication.service.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisPedidosViewModel(application: Application) : AndroidViewModel(application)  {
    private val _misPedidos = MutableLiveData<List<MisPedidos>>()
    val misPedidos: LiveData<List<MisPedidos>> = _misPedidos

    private val _misDetallePedidos = MutableLiveData<List<MisDetallePedidos>>()
    val misDetallePedidos: LiveData<List<MisDetallePedidos>> = _misDetallePedidos

    fun obtenerMisPedidos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val call = RetrofitInstance.getRetrofitAuth(getApplication()).obtenerMisPedidos()
                call.enqueue(object : Callback<List<MisPedidos>> {
                    override fun onResponse(call: Call<List<MisPedidos>>, response: Response<List<MisPedidos>>) {
                        if (response.isSuccessful) {
                            _misPedidos.postValue(response.body())
                        } else {
                            // Manejar error
                        }
                    }

                    override fun onFailure(call: Call<List<MisPedidos>>, t: Throwable) {
                        // Manejar error
                    }
                })
            }
        }
    }

    fun obtenerMisDetallePedidos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val call = RetrofitInstance.getRetrofitAuth(getApplication()).obtenerMisDetallePedidos()
                call.enqueue(object : Callback<List<MisDetallePedidos>> {
                    override fun onResponse(call: Call<List<MisDetallePedidos>>, response: Response<List<MisDetallePedidos>>) {
                        if (response.isSuccessful) {
                            _misDetallePedidos.postValue(response.body())
                        } else {
                            // Manejar error
                        }
                    }

                    override fun onFailure(call: Call<List<MisDetallePedidos>>, t: Throwable) {
                        // Manejar error
                    }
                })
            }
        }
    }


}