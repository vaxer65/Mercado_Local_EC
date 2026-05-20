package com.example.holamundo.screens.productor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.Colores
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.example.holamundo.viewmodel.PedidosViewModel

private val VerdePrincipal = Colores.VerdePrincipal
private val FondoOscuro    = Colores.FondoOscuro
private val FondoTarjeta   = Colores.FondoTarjeta
private val GrisTexto      = Colores.GrisTexto
private val Blanco         = Colores.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelProductorScreen(
    authViewModel: AuthViewModel,
    catalogoViewModel: CatalogoViewModel,
    pedidosViewModel: PedidosViewModel,
    onIrAAgregarProducto: () -> Unit,
    onIrAlChat: (String) -> Unit, // chatId
    onCerrarSesionExito: () -> Unit
) {
    val productorId = authViewModel.usuarioActualUid ?: ""
    val productorNombre = authViewModel.usuarioActualNombre ?: "Productor"

    // Cargar los pedidos del productor
    LaunchedEffect(productorId) {
        if (productorId.isNotEmpty()) {
            pedidosViewModel.cargarPedidosProductor(productorId)
        }
    }

    // Observar productos del productor
    val todosProductos by catalogoViewModel.productosFiltrados.collectAsState()
    val misProductos = todosProductos.filter { it.productorId == productorId }

    // Observar pedidos
    val pedidosRecibidos by pedidosViewModel.pedidosProductor.collectAsState()

    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val titulosTabs = listOf("Mis Productos", "Pedidos", "Chats")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel Productor",
                            color = Blanco,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = productorNombre,
                            color = Color(0xFF9FE1CB),
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.cerrarSesion()
                        onCerrarSesionExito()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = Blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrincipal)
            )
        },
        floatingActionButton = {
            if (tabSeleccionada == 0) {
                FloatingActionButton(
                    onClick = onIrAAgregarProducto,
                    containerColor = VerdePrincipal,
                    contentColor = Blanco
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoOscuro)
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = FondoTarjeta,
                contentColor = VerdePrincipal,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                        color = VerdePrincipal
                    )
                }
            ) {
                titulosTabs.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = tabSeleccionada == indice,
                        onClick = { tabSeleccionada = indice },
                        text = {
                            Text(
                                text = titulo,
                                fontWeight = if (tabSeleccionada == indice) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (tabSeleccionada == indice) VerdePrincipal else GrisTexto
                            )
                        }
                    )
                }
            }

            // Contenido según Tab
            Box(modifier = Modifier.fillMaxSize()) {
                when (tabSeleccionada) {
                    0 -> PestañaMisProductos(productos = misProductos)
                    1 -> PestañaPedidosRecibidos(
                        pedidos = pedidosRecibidos,
                        onAceptarPedido = { id ->
                            pedidosViewModel.actualizarEstadoPedido(id, "aceptado")
                        },
                        onEntregarPedido = { id ->
                            pedidosViewModel.actualizarEstadoPedido(id, "entregado")
                        },
                        onChatClick = { clienteId ->
                            val chatId = "${clienteId}_${productorId}"
                            onIrAlChat(chatId)
                        }
                    )
                    2 -> PestañaChatsActivos(
                        userId = productorId,
                        pedidos = pedidosRecibidos,
                        onChatClick = { clienteId ->
                            val chatId = "${clienteId}_${productorId}"
                            onIrAlChat(chatId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PestañaMisProductos(productos: List<Producto>) {
    if (productos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌱", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tienes productos publicados",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Presiona el botón '+' para agregar el primero.",
                color = GrisTexto,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(productos) { producto ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VerdePrincipal.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = producto.emoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = producto.nombre, color = Blanco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Stock: ${producto.stock} ${producto.unidad}", color = GrisTexto, fontSize = 12.sp)
                            Text(text = "Categoría: ${producto.categoria}", color = GrisTexto, fontSize = 11.sp)
                        }
                        Text(
                            text = "$${String.format("%.2f", producto.precio)}",
                            color = VerdePrincipal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PestañaPedidosRecibidos(
    pedidos: List<Pedido>,
    onAceptarPedido: (Int) -> Unit,
    onEntregarPedido: (Int) -> Unit,
    onChatClick: (String) -> Unit
) {
    if (pedidos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📋", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No has recibido ningún pedido",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Los pedidos de los clientes aparecerán aquí.",
                color = GrisTexto,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(pedidos) { pedido ->
                val (colorEstado, textoEstado) = when (pedido.estado.lowercase()) {
                    "pendiente" -> Pair(Color(0xFFFFB300), "Pendiente")
                    "aceptado"  -> Pair(Color(0xFF29B6F6), "Aceptado")
                    "entregado" -> Pair(Color(0xFF66BB6A), "Entregado")
                    else        -> Pair(GrisTexto, pedido.estado)
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Pedido #${pedido.id}", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colorEstado.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = textoEstado, color = colorEstado, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = pedido.productoEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = pedido.productoNombre, color = Blanco, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Cantidad: ${pedido.cantidad} · Total: $${String.format("%.2f", pedido.total)}", color = GrisTexto, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de Chat
                            Button(
                                onClick = { onChatClick(pedido.consumidorId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E5C)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("💬 Chat", fontSize = 11.sp, color = Blanco, fontWeight = FontWeight.Bold)
                            }

                            // Botón de Acción según Estado
                            if (pedido.estado == "pendiente") {
                                Button(
                                    onClick = { onAceptarPedido(pedido.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Aceptar Pedido", fontSize = 11.sp, color = Blanco, fontWeight = FontWeight.Bold)
                                }
                            } else if (pedido.estado == "aceptado") {
                                Button(
                                    onClick = { onEntregarPedido(pedido.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Marcar Entregado", fontSize = 11.sp, color = Blanco, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PestañaChatsActivos(
    userId: String,
    pedidos: List<Pedido>,
    onChatClick: (String) -> Unit
) {
    // Para simplificar, extraemos los clientes únicos que le han hecho pedidos
    val clientesUnicos = pedidos.map { it.consumidorId }.distinct()

    if (clientesUnicos.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💬", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay chats activos",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Los chats con tus clientes aparecerán aquí.",
                color = GrisTexto,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(clientesUnicos) { clienteId ->
                // Buscar algún pedido de este cliente para mostrar el nombre del producto de referencia
                val pedidoRef = pedidos.firstOrNull { it.consumidorId == clienteId }
                val nombreProducto = pedidoRef?.productoNombre ?: "Producto"

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChatClick(clienteId) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(VerdePrincipal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👤", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Cliente (#${clienteId.takeLast(5)})", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Referencia: $nombreProducto", color = GrisTexto, fontSize = 12.sp)
                        }
                        Text(text = "Ver Chat ›", color = VerdePrincipal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
