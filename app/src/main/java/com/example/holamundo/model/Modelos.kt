package com.example.holamundo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Roles posibles en la app
enum class RolUsuario {
    VISITANTE,
    CONSUMIDOR,
    PRODUCTOR
}

// Estado de autenticación global
sealed class EstadoAuth {
    object Cargando    : EstadoAuth()
    object Visitante   : EstadoAuth()
    data class Logueado(
        val uid: String,
        val nombre: String,
        val email: String,
        val rol: RolUsuario
    ) : EstadoAuth()
}

// Modelo de usuario para la DB de Room
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val rol: String = "" // "CONSUMIDOR" o "PRODUCTOR"
)

// Modelo de producto
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val id: String = "",
    val nombre: String = "",
    val productor: String = "", // Nombre de la finca/productor
    val precio: Double = 0.0,
    val unidad: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val emoji: String = "",
    val stock: Int = 0,
    val provincia: String = "",
    val imagenUrl: String = "",
    val productorId: String = "" // ID de usuario del productor propietario
)

// Modelo de pedido
@Entity(tableName = "pedidos")
data class Pedido(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productoId: String = "",
    val productoNombre: String = "",
    val productoEmoji: String = "",
    val consumidorId: String = "",
    val productorId: String = "",
    val cantidad: Int = 0,
    val total: Double = 0.0,
    val estado: String = "pendiente" // "pendiente", "aceptado", "entregado"
)

// Modelo de mensaje de chat
@Entity(tableName = "mensajes")
data class Mensaje(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String = "", // "consumidorId_productorId"
    val emisorId: String = "",
    val texto: String = "",
    val timestamp: Long = 0L
)