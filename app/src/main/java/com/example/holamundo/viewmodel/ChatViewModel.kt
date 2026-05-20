package com.example.holamundo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.holamundo.model.Mensaje
import com.example.holamundo.model.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val mensajeDao = AppDatabase.getDatabase(application).mensajeDao()

    private val _mensajes = MutableStateFlow<List<Mensaje>>(emptyList())
    val mensajes: StateFlow<List<Mensaje>> = _mensajes.asStateFlow()

    // Cargar y observar mensajes del chat seleccionado
    fun cargarMensajes(chatId: String) {
        viewModelScope.launch {
            mensajeDao.obtenerMensajesChat(chatId).collect { lista ->
                _mensajes.value = lista
            }
        }
    }

    // Insertar un nuevo mensaje en la DB de Room
    fun enviarMensaje(chatId: String, emisorId: String, texto: String) {
        if (texto.isBlank()) return
        viewModelScope.launch {
            val nuevoMensaje = Mensaje(
                chatId = chatId,
                emisorId = emisorId,
                texto = texto,
                timestamp = System.currentTimeMillis()
            )
            mensajeDao.insertar(nuevoMensaje)
        }
    }
}
