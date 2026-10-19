package com.example.holamundo.screens.home

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.Colores
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel

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
    catalogoViewModel: CatalogoViewModel,
    onVolver: () -> Unit,
    onIrALogin: () -> Unit,
    onHacerPedido: (Producto, Int) -> Unit,
    onContactarProductor: (String) -> Unit = {},
    onVerEnAR: (String) -> Unit = {}
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
            .statusBarsPadding()
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

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Realidad Aumentada (AR)
            Button(
                onClick = { onVerEnAR(producto.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E3E5C)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "👀  Ver en AR (Realidad Aumentada)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            }

            if (producto.ubicacionEsReal) {
                Spacer(modifier = Modifier.height(20.dp))
                val context = LocalContext.current
                
                Text(
                    text = "Ubicación de Origen GPS",
                    color = Blanco,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🛡️", fontSize = 16.sp)
                            Text(
                                text = "Ubicación Verificada: Sin simuladores / Ecuador",
                                color = Color(0xFF66BB6A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = producto.direccionVerificada,
                            color = Blanco,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lat: ${producto.latitud}, Lon: ${producto.longitud}",
                            color = GrisTexto,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Button(
                            onClick = {
                                val uri = "geo:${producto.latitud},${producto.longitud}?q=${producto.latitud},${producto.longitud}(${Uri.encode(producto.productor)})"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val browserUri = "https://www.openstreetmap.org/?mlat=${producto.latitud}&mlon=${producto.longitud}#map=16/${producto.latitud}/${producto.longitud}"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(browserUri)))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("🗺️ Ver Finca en el Mapa", color = Blanco, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = FondoTarjeta, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // Título de la sección de valoraciones
            Text(
                text = "Valoraciones y comentarios",
                color = Blanco,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Promedio general de valoraciones
            val valoraciones by catalogoViewModel.valoraciones.collectAsState()
            val promedio = if (valoraciones.isNotEmpty()) valoraciones.map { it.calificacion }.average() else 0.0

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (valoraciones.isNotEmpty()) String.format("%.1f", promedio) else "0.0",
                    color = Blanco,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row {
                        repeat(5) { index ->
                            val estrellaPintada = index < promedio.toInt()
                            Text(
                                text = if (estrellaPintada) "⭐" else "☆",
                                fontSize = 16.sp,
                                color = if (estrellaPintada) Color(0xFFFFD700) else GrisTexto
                            )
                        }
                    }
                    Text(
                        text = "Basado en ${valoraciones.size} valoraciones",
                        color = GrisTexto,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Formulario para escribir valoración (sólo para CONSUMIDOR logueado)
            val usuarioActual = authViewModel.estadoAuth.collectAsState().value
            if (usuarioActual is com.example.holamundo.model.EstadoAuth.Logueado && usuarioActual.rol == com.example.holamundo.model.RolUsuario.CONSUMIDOR) {
                var nuevaCalificacion by remember { mutableIntStateOf(5) }
                var nuevoComentario by remember { mutableStateOf("") }
                var mensajeError by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Escribe tu opinión",
                            color = Blanco,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Selector de estrellas
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Calificación: ", color = GrisTexto, fontSize = 13.sp)
                            repeat(5) { index ->
                                val estrella = index + 1
                                Text(
                                    text = if (estrella <= nuevaCalificacion) "★" else "☆",
                                    fontSize = 24.sp,
                                    color = if (estrella <= nuevaCalificacion) Color(0xFFFFD700) else GrisTexto,
                                    modifier = Modifier.clickable { nuevaCalificacion = estrella }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nuevoComentario,
                            onValueChange = { nuevoComentario = it },
                            placeholder = { Text("¿Qué te pareció este producto? Comparte tu experiencia...", color = GrisTexto, fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Blanco, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VerdePrincipal,
                                unfocusedBorderColor = FondoOscuro,
                                focusedContainerColor = FondoOscuro,
                                unfocusedContainerColor = FondoOscuro
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (mensajeError.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = mensajeError, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (nuevoComentario.trim().isEmpty()) {
                                    mensajeError = "Por favor escribe un comentario"
                                    return@Button
                                }
                                catalogoViewModel.agregarValoracion(
                                    productoId = producto.id,
                                    compradorId = usuarioActual.uid,
                                    compradorNombre = usuarioActual.nombre,
                                    calificacion = nuevaCalificacion,
                                    comentario = nuevoComentario.trim()
                                ) { exito ->
                                    if (exito) {
                                        nuevoComentario = ""
                                        nuevaCalificacion = 5
                                        mensajeError = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Enviar reseña", color = Blanco, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (usuarioActual !is com.example.holamundo.model.EstadoAuth.Logueado) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FondoTarjeta)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Inicia sesión para calificar y dejar un comentario.",
                        color = GrisTexto,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de valoraciones
            if (valoraciones.isEmpty()) {
                Text(
                    text = "Aún no hay valoraciones para este producto. ¡Sé el primero en calificar!",
                    color = GrisTexto,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    valoraciones.forEach { valoracion ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = valoracion.compradorNombre,
                                        color = Blanco,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        repeat(5) { index ->
                                            val pintada = index < valoracion.calificacion
                                            Text(
                                                text = if (pintada) "⭐" else "☆",
                                                fontSize = 11.sp,
                                                color = if (pintada) Color(0xFFFFD700) else GrisTexto
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = valoracion.comentario,
                                    color = Blanco,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}