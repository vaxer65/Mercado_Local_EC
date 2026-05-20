package com.example.holamundo.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.AuthViewModel

private val VerdePrincipal = Colores.VerdePrincipal
private val VerdeSuave     = Colores.VerdeSuave
private val AmarilloSuave  = Colores.AmarilloSuave
private val RojoSuave      = Colores.RojoSuave
private val FondoOscuro    = Colores.FondoOscuro
private val FondoTarjeta   = Colores.FondoTarjeta
private val Blanco         = Colores.Blanco
private val GrisTexto      = Colores.GrisTexto

@Composable
fun DetalleProductoScreen(
    producto: Producto,
    authViewModel: AuthViewModel,
    onVolver: () -> Unit,
    onIrALogin: () -> Unit,
    onHacerPedido: (Producto, Int) -> Unit,
    onContactarProductor: (String) -> Unit = {}
) {
    var cantidad by remember { mutableIntStateOf(1) }
    var favorito by remember { mutableStateOf(false) }

    val colorFondo = when (producto.categoria) {
        "Frutas"     -> RojoSuave
        "Verduras"   -> VerdeSuave
        "Lácteos"    -> AmarilloSuave
        "Artesanías" -> Color(0xFFEEEDFE)
        else         -> Color(0xFFE6F1FB)
    }

    val totalPrecio = producto.precio * cantidad

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(rememberScrollState())
    ) {
        // Zona de imagen grande
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(colorFondo)
        ) {
            // Emoji grande
            Text(
                text = producto.emoji,
                fontSize = 96.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            // Botón volver
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FondoOscuro.copy(alpha = 0.6f))
                    .clickable { onVolver() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Blanco
                )
            }

            // Botón favorito
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FondoOscuro.copy(alpha = 0.6f))
                    .clickable { favorito = !favorito }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (favorito) "❤️" else "🤍",
                    fontSize = 18.sp
                )
            }

            // Badge de stock
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FondoOscuro.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Stock: ${producto.stock} ${producto.unidad}s",
                    color = Blanco,
                    fontSize = 12.sp
                )
            }
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Nombre y provincia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        color = Blanco,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${producto.productor} · ${producto.provincia}",
                        color = GrisTexto,
                        fontSize = 13.sp
                    )
                }
                // Badge categoría
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VerdePrincipal.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = producto.categoria,
                        color = VerdePrincipal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Precio
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$${String.format("%.2f", producto.precio)}",
                    color = VerdePrincipal,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " / ${producto.unidad}",
                    color = GrisTexto,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descripción
            Text(
                text = "Descripción",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = producto.descripcion,
                color = GrisTexto,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de cantidad
            Text(
                text = "Cantidad",
                color = Blanco,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón menos
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FondoTarjeta)
                        .clickable { if (cantidad > 1) cantidad-- },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        color = if (cantidad > 1) Blanco else GrisTexto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$cantidad",
                    color = Blanco,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Botón más
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FondoTarjeta)
                        .clickable { if (cantidad < producto.stock) cantidad++ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = if (cantidad < producto.stock) Blanco else GrisTexto,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Stock: ${producto.stock}",
                    color = GrisTexto,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón hacer pedido
            Button(
                onClick = {
                    if (authViewModel.estaLogueado) {
                        onHacerPedido(producto, cantidad)
                    } else {
                        onIrALogin()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (authViewModel.estaLogueado)
                        "Hacer pedido — $${String.format("%.2f", totalPrecio)}"
                    else
                        "Inicia sesión para pedir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón contactar productor
            OutlinedButton(
                onClick = {
                    if (authViewModel.estaLogueado) {
                        onContactarProductor(producto.productorId)
                    } else {
                        onIrALogin()
                    }
                },
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdePrincipal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "💬  Contactar al productor",
                    fontSize = 14.sp,
                    color = VerdePrincipal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}