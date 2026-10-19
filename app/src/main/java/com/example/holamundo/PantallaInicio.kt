package com.example.holamundo

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.model.EstadoAuth
import com.example.holamundo.model.RolUsuario
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.example.holamundo.viewmodel.PedidosViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.holamundo.utils.ReportGenerator

private val VerdePrincipal = Color(0xFF1D9E75)
private val VerdeOscuro    = Color(0xFF0F6E56)
private val VerdeSuave     = Color(0xFFE1F5EE)
private val AmarilloSuave  = Color(0xFFFAEEDA)
private val RojoSuave      = Color(0xFFFAECE7)
private val GrisFondo      = Color(0xFF1A1A2E)
private val GrisTexto      = Color(0xFF9E9E9E)
private val Blanco         = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    authViewModel: AuthViewModel,
    catalogoViewModel: CatalogoViewModel,
    onIrALogin: () -> Unit = {},
    onIrACatalogo: () -> Unit = {},
    onIrAPedidos: () -> Unit = {},
    onIrAPerfil: () -> Unit = {},
    onIrAEscaner: () -> Unit = {},
    onProductoClick: (Producto) -> Unit = {}
) {
    val context = LocalContext.current
    val productos by catalogoViewModel.productosFiltrados.collectAsState()
    val estadoAuth by authViewModel.estadoAuth.collectAsState()
    val pedidosViewModel: PedidosViewModel = viewModel()

    val destacados = productos.take(4)
    val artesanias = productos.filter { it.categoria == "Artesanías" }

    var mostrarDialogoPerfil by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            HeaderApp(
                estadoAuth = estadoAuth,
                onIrALogin = onIrALogin,
                onCarritoClick = {
                    if (authViewModel.estaLogueado) onIrAPedidos() else onIrALogin()
                }
            )

            // Banner ilustrativo animado de MercadoLocal EC
            var bannerVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                bannerVisible = true
            }
            val bannerAlpha by animateFloatAsState(
                targetValue = if (bannerVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 1000),
                label = "bannerAlpha"
            )
            val bannerScaleEntry by animateFloatAsState(
                targetValue = if (bannerVisible) 1f else 0.9f,
                animationSpec = tween(durationMillis = 1000),
                label = "bannerScaleEntry"
            )

            // Animación continua de respiración para dar un tono animado continuo
            val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.025f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            val finalScale = bannerScaleEntry * pulseScale

            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .graphicsLayer(
                        alpha = bannerAlpha,
                        scaleX = finalScale,
                        scaleY = finalScale
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.market_banner),
                    contentDescription = "Mercado Local Ecuador",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

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

            // Barra de búsqueda que redirige al catálogo o escáner
            BarraBusquedaInerte(
                onSearchClick = onIrACatalogo,
                onEscanerClick = onIrAEscaner
            )

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

    // ModalBottomSheet moderno de perfil profesional
    if (mostrarDialogoPerfil) {
        val loggedInUser = estadoAuth as? EstadoAuth.Logueado
        if (loggedInUser != null) {
            var modoEdicion by remember { mutableStateOf(false) }
            var nombreEditado by remember(loggedInUser) { mutableStateOf(loggedInUser.nombre) }
            var emailEditado by remember(loggedInUser) { mutableStateOf(loggedInUser.email) }
            var mensajeError by remember { mutableStateOf("") }
            var guardando by remember { mutableStateOf(false) }

            // Cargar y observar estadísticas reales de compras/ventas
            LaunchedEffect(loggedInUser.uid) {
                if (loggedInUser.rol == RolUsuario.PRODUCTOR) {
                    pedidosViewModel.cargarPedidosProductor(loggedInUser.uid)
                } else {
                    pedidosViewModel.cargarPedidosConsumidor(loggedInUser.uid)
                }
            }

            val pedidosConsumidor by pedidosViewModel.pedidosConsumidor.collectAsState()
            val pedidosProductor by pedidosViewModel.pedidosProductor.collectAsState()

            ModalBottomSheet(
                onDismissRequest = {
                    mostrarDialogoPerfil = false
                    modoEdicion = false
                    mensajeError = ""
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1E1E2F),
                dragHandle = { BottomSheetDefaults.DragHandle(color = VerdePrincipal) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circular profesional con degradado
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(VerdePrincipal, Color(0xFF0F6E56))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loggedInUser.nombre.take(1).uppercase(),
                            color = Blanco,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (modoEdicion) {
                        // Formulario de edición con estilo moderno
                        OutlinedTextField(
                            value = nombreEditado,
                            onValueChange = { nombreEditado = it },
                            label = { Text("Nombre Completo", color = VerdePrincipal) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdePrincipal,
                                unfocusedBorderColor = Color(0xFF2A2A3E),
                                focusedLabelColor = VerdePrincipal,
                                cursorColor = VerdePrincipal,
                                focusedTextColor = Blanco,
                                unfocusedTextColor = Blanco
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = emailEditado,
                            onValueChange = { emailEditado = it },
                            label = { Text("Correo Electrónico", color = VerdePrincipal) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdePrincipal,
                                unfocusedBorderColor = Color(0xFF2A2A3E),
                                focusedLabelColor = VerdePrincipal,
                                cursorColor = VerdePrincipal,
                                focusedTextColor = Blanco,
                                unfocusedTextColor = Blanco
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (mensajeError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = mensajeError, color = Color(0xFFE24B4A), fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    modoEdicion = false
                                    nombreEditado = loggedInUser.nombre
                                    emailEditado = loggedInUser.email
                                    mensajeError = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A3E))
                            ) {
                                Text("Cancelar", color = Blanco)
                            }

                            Button(
                                onClick = {
                                    if (nombreEditado.isBlank() || emailEditado.isBlank()) {
                                        mensajeError = "Los campos no pueden estar vacíos"
                                        return@Button
                                    }
                                    guardando = true
                                    authViewModel.actualizarPerfil(
                                        uid = loggedInUser.uid,
                                        nuevoNombre = nombreEditado.trim(),
                                        nuevoEmail = emailEditado.trim()
                                    ) { exito, error ->
                                        guardando = false
                                        if (exito) {
                                            modoEdicion = false
                                            mensajeError = ""
                                        } else {
                                            mensajeError = error
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                enabled = !guardando
                            ) {
                                Text(if (guardando) "Guardando..." else "Guardar", color = Blanco)
                            }
                        }
                    } else {
                        // Vista estática de Nombre y Correo
                        Text(
                            text = loggedInUser.nombre,
                            color = Blanco,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = loggedInUser.email,
                            color = GrisTexto,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Badge de rol con diseño moderno
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(VerdePrincipal.copy(alpha = 0.15f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (loggedInUser.rol == RolUsuario.PRODUCTOR) "👨‍🌾 Productor Local" else "🛒 Consumidor",
                                color = VerdePrincipal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFF2A2A3E), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Estadísticas de perfil del usuario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (loggedInUser.rol == RolUsuario.PRODUCTOR) "Ubicación" else "Cuenta",
                                    color = GrisTexto,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (loggedInUser.rol == RolUsuario.PRODUCTOR) "Imbabura, EC" else "Verificada",
                                    color = Blanco,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (loggedInUser.rol == RolUsuario.PRODUCTOR) "Ventas" else "Compras",
                                    color = GrisTexto,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (loggedInUser.rol == RolUsuario.PRODUCTOR) {
                                        val udsVendidas = pedidosProductor.filter { it.estado == "entregado" || it.estado == "aceptado" }.sumOf { it.cantidad }
                                        "$udsVendidas uds"
                                    } else {
                                        "${pedidosConsumidor.size} pedidos"
                                    },
                                    color = Blanco,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón para ingresar al modo edición
                        Button(
                            onClick = { modoEdicion = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
                        ) {
                            Text("Editar Perfil", fontSize = 14.sp, color = Blanco)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón para generar Reporte PDF
                        Button(
                            onClick = {
                                if (loggedInUser.rol == RolUsuario.PRODUCTOR) {
                                    val misProductos = productos.filter { it.productorId == loggedInUser.uid }
                                    ReportGenerator.generarReporteProductor(
                                        context = context,
                                        productorNombre = loggedInUser.nombre,
                                        productorEmail = loggedInUser.email,
                                        productos = misProductos,
                                        pedidos = pedidosProductor
                                    )
                                } else {
                                    ReportGenerator.generarReporteConsumidor(
                                        context = context,
                                        consumidorNombre = loggedInUser.nombre,
                                        consumidorEmail = loggedInUser.email,
                                        pedidos = pedidosConsumidor
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E5C))
                        ) {
                            Text("📄 Descargar Reporte PDF", fontSize = 14.sp, color = Blanco)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón para cerrar sesión
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                authViewModel.cerrarSesion()
                                mostrarDialogoPerfil = false
                                modoEdicion = false
                                mensajeError = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE24B4A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE24B4A))
                    ) {
                        Text("Cerrar Sesión", color = Color(0xFFE24B4A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
fun BarraBusquedaInerte(onSearchClick: () -> Unit, onEscanerClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            enabled = true,
            placeholder = { Text("Buscar productos locales...", color = GrisTexto) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = GrisTexto) },
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = Color.Transparent,
                unfocusedBorderColor    = Color.Transparent,
                focusedContainerColor   = Color(0xFF2A2A3E),
                unfocusedContainerColor = Color(0xFF2A2A3E),
                disabledContainerColor  = Color(0xFF2A2A3E),
                disabledPlaceholderColor = GrisTexto
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Overlay transparente para cliquear la barra de búsqueda y buscar
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 55.dp)
                .clickable { onSearchClick() }
        )

        // Botón del escáner en la derecha
        IconButton(
            onClick = onEscanerClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Escáner",
                tint = VerdePrincipal
            )
        }
    }
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