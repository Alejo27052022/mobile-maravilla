package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Mesa
import com.example.myapplication.service.RetrofitInstance
import kotlinx.coroutines.launch

class MesaViewModel(application: Application) : AndroidViewModel(application) {

    private val _mesas = MutableLiveData<List<Mesa>>()
    val mesas: LiveData<List<Mesa>> = _mesas

    private val _mesaDetalle = MutableLiveData<Mesa>()
    val mesaDetalle: LiveData<Mesa> = _mesaDetalle

    fun obtenerMesas() {
        viewModelScope.launch {
            try {
                val mesas = RetrofitInstance.getRetrofitAuth(getApplication()).getMesas()
                _mesas.postValue(mesas)
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    fun obtenerMesaDetalle(id: Int) {
        viewModelScope.launch {
            try {
                val mesa = RetrofitInstance.getRetrofitAuth(getApplication()).getMesaById(id)
                _mesaDetalle.postValue(mesa)
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }
}