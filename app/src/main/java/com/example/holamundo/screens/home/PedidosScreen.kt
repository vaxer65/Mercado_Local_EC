package com.example.holamundo.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.BarraNavegacionInferior
import com.example.holamundo.model.Pedido
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.PedidosViewModel

import com.example.holamundo.Colores

private val Oro            = Color(0xFFFFB300)
private val AzulInfo       = Color(0xFF29B6F6)
private val VerdeExito     = Color(0xFF66BB6A)
private val GrisClaro      = Color(0xFFCCCCCC)
private val VerdePrincipal = Colores.VerdePrincipal
private val FondoOscuro    = Colores.FondoOscuro
private val FondoTarjeta   = Colores.FondoTarjeta
private val GrisTexto      = Colores.GrisTexto
private val Blanco         = Colores.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    authViewModel: AuthViewModel,
    pedidosViewModel: PedidosViewModel,
    onVolverAlHome: () -> Unit,
    onIrACatalogo: () -> Unit,
    onContactarProductor: (String, String) -> Unit // productorId, productorNombre
) {
    val consumidorId = authViewModel.usuarioActualUid ?: ""
    val pedidos by pedidosViewModel.pedidosConsumidor.collectAsState()

    // Cargar los pedidos al entrar
    LaunchedEffect(consumidorId) {
        if (consumidorId.isNotEmpty()) {
            pedidosViewModel.cargarPedidosConsumidor(consumidorId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolverAlHome) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrincipal)
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                onIrAHome = onVolverAlHome,
                onIrACatalogo = onIrACatalogo,
                onIrAPedidos = {},
                onIrAPerfil = { /* Trigger profile dialog in home or handle here */ },
                indiceSeleccionado = 2
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoOscuro)
                .padding(paddingValues)
        ) {
            if (pedidos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📦", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aún no tienes pedidos",
                        color = Blanco,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Explora el catálogo y apoya a los productores locales.",
                        color = GrisTexto,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onIrACatalogo() }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pedidos) { pedido ->
                        TarjetaPedidoConsumidor(
                            pedido = pedido,
                            onChatClick = {
                                onContactarProductor(pedido.productorId, "Productor")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaPedidoConsumidor(
    pedido: Pedido,
    onChatClick: () -> Unit
) {
    val (colorEstado, textoEstado) = when (pedido.estado.lowercase()) {
        "pendiente" -> Pair(Oro, "Pendiente")
        "aceptado"  -> Pair(AzulInfo, "Aceptado")
        "entregado" -> Pair(VerdeExito, "Entregado")
        else        -> Pair(GrisTexto, pedido.estado.replaceFirstChar { it.uppercase() })
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera de la tarjeta: Nro pedido y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    color = Blanco,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorEstado.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = Color(0xFF3A3A5E), modifier = Modifier.padding(vertical = 12.dp))

            // Detalle del producto
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorEstado.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pedido.productoEmoji.ifEmpty { "📦" }, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pedido.productoNombre,
                        color = Blanco,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Cantidad: ${pedido.cantidad} unidades",
                        color = GrisTexto,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", pedido.total)}",
                        color = VerdePrincipal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón chatear con productor
            OutlinedButton(
                onClick = onChatClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdePrincipal),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Text(
                    text = "💬 Consultar al productor",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdePrincipal
                )
            }
        }
    }
}
