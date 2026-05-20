package com.example.holamundo.screens.productor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.Colores
import com.example.holamundo.model.DatosPrueba
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel

private val Blanco         = Colores.Blanco
private val VerdePrincipal = Colores.VerdePrincipal
private val FondoOscuro    = Colores.FondoOscuro
private val FondoTarjeta   = Colores.FondoTarjeta
private val GrisTexto      = Colores.GrisTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(
    authViewModel: AuthViewModel,
    catalogoViewModel: CatalogoViewModel,
    onVolver: () -> Unit,
    onAgregarExito: () -> Unit
) {
    val productorId = authViewModel.usuarioActualUid ?: ""
    val productorNombre = authViewModel.usuarioActualNombre ?: "Finca Local"

    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("unidad") }
    var stockStr by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Selectores
    var categoriaSeleccionada by remember { mutableStateOf("Frutas") }
    var provinciaSeleccionada by remember { mutableStateOf("Imbabura") }
    var emojiSeleccionado by remember { mutableStateOf("🍎") }

    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedProvincia by remember { mutableStateOf(false) }

    var errorMensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val listaEmojis = listOf("🥑", "🍓", "🥕", "🧀", "🍋", "🧅", "🧶", "🏺", "🍯", "🥔", "🍍", "🍌", "🍎", "🥦", "🌽")

    // Filtrar "Todos" y "Todas" de las listas de DatosPrueba
    val categorias = DatosPrueba.categorias.filter { it != "Todos" }
    val provincias = DatosPrueba.provincias.filter { it != "Todas" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publicar Producto", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrincipal)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoOscuro)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Nombre
                    Text("Nombre del producto", color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; errorMensaje = "" },
                        placeholder = { Text("Ej: Mango de chupar", color = GrisTexto) },
                        shape = RoundedCornerShape(12.dp),
                        colors = campColores(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Precio y Unidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Precio ($)", color = GrisTexto, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = precioStr,
                                onValueChange = { precioStr = it; errorMensaje = "" },
                                placeholder = { Text("1.50", color = GrisTexto) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = campColores(),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Unidad de venta", color = GrisTexto, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = unidad,
                                onValueChange = { unidad = it; errorMensaje = "" },
                                placeholder = { Text("libra, kg, unidad", color = GrisTexto) },
                                shape = RoundedCornerShape(12.dp),
                                colors = campColores(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stock y Categoría
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stock inicial", color = GrisTexto, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = stockStr,
                                onValueChange = { stockStr = it; errorMensaje = "" },
                                placeholder = { Text("50", color = GrisTexto) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = campColores(),
                                singleLine = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Categoría", color = GrisTexto, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = expandedCategoria,
                                onExpandedChange = { expandedCategoria = !expandedCategoria }
                            ) {
                                OutlinedTextField(
                                    value = categoriaSeleccionada,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = campColores(),
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedCategoria,
                                    onDismissRequest = { expandedCategoria = false },
                                    modifier = Modifier.background(FondoTarjeta)
                                ) {
                                    categorias.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = Blanco) },
                                            onClick = {
                                                categoriaSeleccionada = cat
                                                expandedCategoria = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Provincia
                    Text("Provincia de producción", color = GrisTexto, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedProvincia,
                        onExpandedChange = { expandedProvincia = !expandedProvincia }
                    ) {
                        OutlinedTextField(
                            value = provinciaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvincia) },
                            shape = RoundedCornerShape(12.dp),
                            colors = campColores(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProvincia,
                            onDismissRequest = { expandedProvincia = false },
                            modifier = Modifier.background(FondoTarjeta)
                        ) {
                            provincias.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov, color = Blanco) },
                                    onClick = {
                                        provinciaSeleccionada = prov
                                        expandedProvincia = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selector de Emoji
                    Text("Selecciona un icono ilustrativo", color = GrisTexto, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(VerdePrincipal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emojiSeleccionado, fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        // Cuadrícula simple de selección
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val chunks = listaEmojis.chunked(7)
                            chunks.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    row.forEach { emo ->
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (emojiSeleccionado == emo) VerdePrincipal else Color(0xFF1A1A2E))
                                                .clickable { emojiSeleccionado = emo }
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(emo, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Descripción
                    Text("Descripción del producto", color = GrisTexto, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it; errorMensaje = "" },
                        placeholder = { Text("Escribe sobre el cultivo, origen o beneficios de tu producto...", color = GrisTexto) },
                        shape = RoundedCornerShape(12.dp),
                        colors = campColores(),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error
                    if (errorMensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMensaje,
                            color = Color(0xFFE24B4A),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Guardar
                    Button(
                        onClick = {
                            val precio = precioStr.toDoubleOrNull()
                            val stock = stockStr.toIntOrNull()
                            when {
                                nombre.isBlank() -> errorMensaje = "Ingresa el nombre del producto"
                                precio == null || precio <= 0 -> errorMensaje = "Ingresa un precio válido (mayor a 0)"
                                unidad.isBlank() -> errorMensaje = "Ingresa la unidad de venta"
                                stock == null || stock < 0 -> errorMensaje = "Ingresa un stock inicial válido"
                                descripcion.isBlank() -> errorMensaje = "Ingresa una descripción del producto"
                                else -> {
                                    cargando = true
                                    catalogoViewModel.agregarProducto(
                                        nombre = nombre,
                                        productorNombre = productorNombre,
                                        productorId = productorId,
                                        precio = precio,
                                        unidad = unidad,
                                        categoria = categoriaSeleccionada,
                                        stock = stock,
                                        provincia = provinciaSeleccionada,
                                        emoji = emojiSeleccionado,
                                        descripcion = descripcion
                                    ) { exito ->
                                        cargando = false
                                        if (exito) {
                                            onAgregarExito()
                                        } else {
                                            errorMensaje = "Error al registrar el producto"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !cargando,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Blanco, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Guardar y Publicar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blanco)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun campColores() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor    = Color(0xFF3A3A5E),
    focusedBorderColor      = VerdePrincipal,
    unfocusedContainerColor = Color(0xFF1A1A2E),
    focusedContainerColor   = Color(0xFF1A1A2E),
    unfocusedTextColor      = Blanco,
    focusedTextColor        = Blanco
)
