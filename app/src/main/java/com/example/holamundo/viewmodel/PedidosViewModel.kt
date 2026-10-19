package com.example.holamundo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val pedidoDao = AppDatabase.getDatabase(application).pedidoDao()
    private val productoDao = AppDatabase.getDatabase(application).productoDao()

    // Pedidos del consumidor actual
    private val _pedidosConsumidor = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosConsumidor: StateFlow<List<Pedido>> = _pedidosConsumidor.asStateFlow()

    // Pedidos del productor actual
    private val _pedidosProductor = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosProductor: StateFlow<List<Pedido>> = _pedidosProductor.asStateFlow()

    // Cargar pedidos para consumidor
    fun cargarPedidosConsumidor(consumidorId: String) {
        viewModelScope.launch {
            pedidoDao.obtenerPorConsumidor(consumidorId).collect { lista ->
                _pedidosConsumidor.value = lista
            }
        }
    }

    // Cargar pedidos para productor
    fun cargarPedidosProductor(productorId: String) {
        viewModelScope.launch {
            pedidoDao.obtenerPorProductor(productorId).collect { lista ->
                _pedidosProductor.value = lista
            }
        }
    }

    // Crear un nuevo pedido (compra directa) y descontar stock
    fun realizarPedido(
        productoId: String,
        consumidorId: String,
        cantidad: Int,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val producto = productoDao.obtenerPorId(productoId)
            if (producto == null) {
                onResultado(false, "El producto ya no existe")
                return@launch
            }

            if (producto.stock < cantidad) {
                onResultado(false, "No hay suficiente stock disponible. Disponible: ${producto.stock}")
                return@launch
            }

            // Descontar stock
            val nuevoStock = producto.stock - cantidad
            productoDao.actualizarStock(productoId, nuevoStock)

            // Registrar pedido
            val total = producto.precio * cantidad
            val nuevoPedido = Pedido(
                productoId = producto.id,
                productoNombre = producto.nombre,
                productoEmoji = producto.emoji,
                productoCategoria = producto.categoria,
                consumidorId = consumidorId,
                productorId = producto.productorId,
                cantidad = cantidad,
                total = total,
                estado = "pendiente" // estados: pendiente, aceptado, entregado
            )
            pedidoDao.insertar(nuevoPedido)
            onResultado(true, "Pedido realizado con éxito por $${String.format("%.2f", total)}")
        }
    }

    // Actualizar el estado del pedido (usado por el productor)
    fun actualizarEstadoPedido(pedidoId: Int, nuevoEstado: String) {
        viewModelScope.launch {
            pedidoDao.actualizarEstado(pedidoId, nuevoEstado)
        }
    }
}
