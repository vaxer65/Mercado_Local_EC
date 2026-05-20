package com.example.holamundo.navigation

sealed class Rutas(val ruta: String) {
    // Pantallas públicas (sin login)
    object Splash       : Rutas("splash")
    object Home         : Rutas("home")
    object Catalogo     : Rutas("catalogo")
    object Detalle      : Rutas("detalle/{productoId}") {
        fun crearRuta(productoId: String) = "detalle/$productoId"
    }

    // Autenticación
    object Login        : Rutas("login")
    object Registro     : Rutas("registro")

    // Pantallas que requieren login
    object HomeCompleto : Rutas("home_completo")
    object MisPedidos   : Rutas("mis_pedidos")
    object Pedido       : Rutas("pedido/{productoId}") {
        fun crearRuta(productoId: String) = "pedido/$productoId"
    }
    object Chat         : Rutas("chat/{chatId}") {
        fun crearRuta(chatId: String) = "chat/$chatId"
    }
    object ListaChats   : Rutas("lista_chats")

    // Productor
    object PanelProductor  : Rutas("panel_productor")
    object AgregarProducto : Rutas("agregar_producto")
}