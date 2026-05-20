package com.example.holamundo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.model.EstadoAuth
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel

private val VerdePrincipal = Color(0xFF1D9E75)
private val VerdeOscuro    = Color(0xFF0F6E56)
private val VerdeSuave     = Color(0xFFE1F5EE)
private val AmarilloSuave  = Color(0xFFFAEEDA)
private val RojoSuave      = Color(0xFFFAECE7)
private val GrisFondo      = Color(0xFF1A1A2E)
private val GrisTexto      = Color(0xFF9E9E9E)
private val Blanco         = Color(0xFFFFFFFF)

@Composable
fun PantallaInicio(
    authViewModel: AuthViewModel,
    catalogoViewModel: CatalogoViewModel,
    onIrALogin: () -> Unit = {},
    onIrACatalogo: () -> Unit = {},
    onIrAPedidos: () -> Unit = {},
    onIrAPerfil: () -> Unit = {},
    onProductoClick: (Producto) -> Unit = {}
) {
    val productos by catalogoViewModel.productosFiltrados.collectAsState()
    val estadoAuth by authViewModel.estadoAuth.collectAsState()

    val destacados = productos.take(4)
    val artesanias = productos.filter { it.categoria == "Artesanías" }

    var mostrarDialogoPerfil by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BarraNavegacionInferior(
                onIrAHome = {},
                onIrACatalogo = onIrACatalogo,
                onIrAPedidos = {
                    if (authViewModel.estaLogueado) {
                        onIrAPedidos()
                    } else {
                        onIrALogin()
                    }
                },
                onIrAPerfil = {
                    if (authViewModel.estaLogueado) {
                        mostrarDialogoPerfil = true
                    } else {
                        onIrALogin()
                    }
                },
                indiceSeleccionado = 0
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GrisFondo)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderApp(
                estadoAuth = estadoAuth,
                onIrALogin = onIrALogin,
                onCarritoClick = {
                    if (authViewModel.estaLogueado) onIrAPedidos() else onIrALogin()
                }
            )

            // Caja de bienvenida personalizada
            if (authViewModel.estaLogueado) {
                val nombre = authViewModel.usuarioActualNombre ?: "Usuario"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A2A3E))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "👋 ¡Hola, $nombre!",
                            color = Blanco,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "¿Qué productos frescos locales del Ecuador buscas hoy?",
                            color = GrisTexto,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Barra de búsqueda que redirige al catálogo
            Box(
                modifier = Modifier
                    .clickable { onIrACatalogo() }
            ) {
                BarraBusquedaInerte()
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sección Destacados
            if (destacados.isNotEmpty()) {
                TextoSeccion("Destacados cerca de ti")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    items(destacados) { producto ->
                        TarjetaProductoHome(
                            producto = producto,
                            onClick = { onProductoClick(producto) }
                        )
                    }
                }
            }

            // Sección Artesanías
            if (artesanias.isNotEmpty()) {
                TextoSeccion("Artesanías del Ecuador")
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    artesanias.forEach { artesania ->
                        TarjetaHorizontalHome(
                            producto = artesania,
                            onClick = { onProductoClick(artesania) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cargando productos locales...",
                        color = GrisTexto,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Diálogo de perfil para cerrar sesión
    if (mostrarDialogoPerfil) {
        val nombre = authViewModel.usuarioActualNombre ?: ""
        val rol = if (authViewModel.esProductor) "Productor" else "Consumidor"
        AlertDialog(
            onDismissRequest = { mostrarDialogoPerfil = false },
            title = { Text("Mi Cuenta", color = Blanco) },
            text = {
                Column {
                    Text("Nombre: $nombre", color = Blanco)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Rol: $rol", color = GrisTexto)
                }
            },
            containerColor = Color(0xFF2A2A3E),
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.cerrarSesion()
                        mostrarDialogoPerfil = false
                    }
                ) {
                    Text("Cerrar Sesión", color = Color(0xFFE24B4A))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPerfil = false }) {
                    Text("Cerrar", color = VerdePrincipal)
                }
            }
        )
    }
}

@Composable
fun HeaderApp(
    estadoAuth: EstadoAuth,
    onIrALogin: () -> Unit = {},
    onCarritoClick: () -> Unit = {}
) {
    val estaLogueado = estadoAuth is EstadoAuth.Logueado
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(VerdePrincipal)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Text(
                text = "MercadoLocal EC",
                color = Blanco,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (estaLogueado) "Comercio justo local" else "Explora sin registrarte",
                color = Color(0xFF9FE1CB),
                fontSize = 13.sp
            )
        }

        if (!estaLogueado) {
            Button(
                onClick = onIrALogin,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeOscuro),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(34.dp)
            ) {
                Text("Ingresar", fontSize = 12.sp, color = Blanco)
            }
        } else {
            IconButton(
                onClick = onCarritoClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Carrito",
                    tint = Blanco
                )
            }
        }
    }
}

@Composable
fun BarraBusquedaInerte() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        enabled = false,
        placeholder = { Text("Buscar productos locales...", color = GrisTexto) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = GrisTexto) },
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor     = Color.Transparent,
            disabledContainerColor  = Color(0xFF2A2A3E),
            disabledPlaceholderColor = GrisTexto
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun TarjetaProductoHome(
    producto: Producto,
    onClick: () -> Unit
) {
    val colorFondo = when (producto.categoria) {
        "Frutas"     -> RojoSuave
        "Verduras"   -> VerdeSuave
        "Lácteos"    -> AmarilloSuave
        "Artesanías" -> Color(0xFFEEEDFE)
        else         -> Color(0xFFE6F1FB)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Text(text = producto.emoji, fontSize = 40.sp)
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = producto.nombre,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Blanco,
                    maxLines = 1
                )
                Text(
                    text = producto.productor,
                    fontSize = 10.sp,
                    color = GrisTexto,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", producto.precio)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdePrincipal
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(VerdePrincipal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = Blanco,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaHorizontalHome(
    producto: Producto,
    onClick: () -> Unit
) {
    val colorFondo = when (producto.categoria) {
        "Frutas"     -> RojoSuave
        "Verduras"   -> VerdeSuave
        "Lácteos"    -> AmarilloSuave
        "Artesanías" -> Color(0xFFEEEDFE)
        else         -> Color(0xFFE6F1FB)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Text(text = producto.emoji, fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Blanco)
                Text(producto.productor, fontSize = 11.sp, color = GrisTexto)
                Text(
                    text = "$${String.format("%.2f", producto.precio)} / ${producto.unidad}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdePrincipal
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Ver más", fontSize = 11.sp, color = Blanco)
            }
        }
    }
}

@Composable
fun TextoSeccion(titulo: String) {
    Text(
        text = titulo,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Blanco,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun BarraNavegacionInferior(
    onIrAHome: () -> Unit = {},
    onIrACatalogo: () -> Unit = {},
    onIrAPedidos: () -> Unit = {},
    onIrAPerfil: () -> Unit = {},
    indiceSeleccionado: Int = 0
) {
    NavigationBar(containerColor = Color(0xFF2A2A3E)) {
        NavigationBarItem(
            selected = indiceSeleccionado == 0,
            onClick = onIrAHome,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdePrincipal,
                selectedTextColor = VerdePrincipal,
                indicatorColor    = Color(0xFF1A1A2E)
            )
        )
        NavigationBarItem(
            selected = indiceSeleccionado == 1,
            onClick = onIrACatalogo,
            icon = { Icon(Icons.Default.Search, contentDescription = "Explorar") },
            label = { Text("Explorar", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdePrincipal,
                selectedTextColor = VerdePrincipal,
                indicatorColor    = Color(0xFF1A1A2E),
                unselectedIconColor = GrisTexto,
                unselectedTextColor = GrisTexto
            )
        )
        NavigationBarItem(
            selected = indiceSeleccionado == 2,
            onClick = onIrAPedidos,
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Pedidos") },
            label = { Text("Pedidos", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdePrincipal,
                selectedTextColor = VerdePrincipal,
                indicatorColor    = Color(0xFF1A1A2E),
                unselectedIconColor = GrisTexto,
                unselectedTextColor = GrisTexto
            )
        )
        NavigationBarItem(
            selected = indiceSeleccionado == 3,
            onClick = onIrAPerfil,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdePrincipal,
                selectedTextColor = VerdePrincipal,
                indicatorColor    = Color(0xFF1A1A2E),
                unselectedIconColor = GrisTexto,
                unselectedTextColor = GrisTexto
            )
        )
    }
}