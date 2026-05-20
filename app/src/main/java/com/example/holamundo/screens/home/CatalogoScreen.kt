package com.example.holamundo.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.holamundo.model.DatosPrueba
import com.example.holamundo.model.Producto
import com.example.holamundo.viewmodel.CatalogoViewModel

private val VerdePrincipal  = Color(0xFF1D9E75)
private val VerdeOscuro     = Color(0xFF0F6E56)
private val FondoOscuro     = Color(0xFF1A1A2E)
private val FondoTarjeta    = Color(0xFF2A2A3E)
private val GrisTexto       = Color(0xFF9E9E9E)
private val Blanco          = Color(0xFFFFFFFF)
private val VerdeSuave      = Color(0xFFE1F5EE)
private val AmarilloSuave   = Color(0xFFFAEEDA)
private val RojoSuave       = Color(0xFFFAECE7)

@Composable
fun CatalogoScreen(
    catalogoViewModel: CatalogoViewModel = viewModel(),
    onProductoClick: (Producto) -> Unit,
    onVolverAlHome: () -> Unit
) {
    val productos by catalogoViewModel.productosFiltrados.collectAsStateWithLifecycle()
    val categoriaActiva by catalogoViewModel.categoriaActiva.collectAsStateWithLifecycle()
    val busqueda by catalogoViewModel.busqueda.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdePrincipal)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Catálogo",
                color = Blanco,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = Blanco,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = busqueda,
            onValueChange = { catalogoViewModel.cambiarBusqueda(it) },
            placeholder = { Text("Buscar productos, productores...", color = GrisTexto, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = GrisTexto) },
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = VerdePrincipal,
                unfocusedContainerColor = FondoTarjeta,
                focusedContainerColor   = FondoTarjeta,
                unfocusedTextColor      = Blanco,
                focusedTextColor        = Blanco
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true
        )

        // Chips de categorías
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(DatosPrueba.categorias) { categoria ->
                val seleccionada = categoria == categoriaActiva
                FilterChip(
                    selected = seleccionada,
                    onClick = { catalogoViewModel.cambiarCategoria(categoria) },
                    label = { Text(categoria, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VerdePrincipal,
                        selectedLabelColor     = Blanco,
                        containerColor         = FondoTarjeta,
                        labelColor             = GrisTexto
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = seleccionada,
                        selectedBorderColor = VerdePrincipal,
                        borderColor = Color(0xFF3A3A5E)
                    )
                )
            }
        }

        // Contador de resultados
        Text(
            text = "${productos.size} productos encontrados",
            color = GrisTexto,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Grid de productos
        if (productos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No encontramos productos",
                        color = Blanco,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Intenta con otra búsqueda",
                        color = GrisTexto,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    TarjetaProductoCatalogo(
                        producto = producto,
                        onClick = { onProductoClick(producto) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaProductoCatalogo(
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Imagen / Emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(colorFondo),
                contentAlignment = Alignment.Center
            ) {
                Text(text = producto.emoji, fontSize = 44.sp)

                // Badge de categoría
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(VerdePrincipal.copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = producto.categoria,
                        color = Blanco,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Badge de stock bajo
                if (producto.stock <= 10) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE24B4A).copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "¡Últimas!",
                            color = Blanco,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = producto.nombre,
                    color = Blanco,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = producto.productor,
                    color = GrisTexto,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "📍 ${producto.provincia}",
                    color = GrisTexto,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$${String.format("%.2f", producto.precio)}",
                            color = VerdePrincipal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/ ${producto.unidad}",
                            color = GrisTexto,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VerdePrincipal)
                            .clickable { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "+", color = Blanco, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}