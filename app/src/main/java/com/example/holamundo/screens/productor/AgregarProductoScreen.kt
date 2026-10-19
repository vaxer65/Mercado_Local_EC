package com.example.holamundo.screens.productor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.holamundo.Colores
import com.example.holamundo.model.DatosPrueba
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.example.holamundo.network.RetrofitClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val productorId = authViewModel.usuarioActualUid ?: ""
    val productorNombre = authViewModel.usuarioActualNombre ?: "Finca Local"

    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("unidad") }
    var stockStr by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    // Geolocation States
    var latitud by remember { mutableStateOf(0.0) }
    var longitud by remember { mutableStateOf(0.0) }
    var direccionVerificada by remember { mutableStateOf("") }
    var ubicacionEsReal by remember { mutableStateOf(false) }
    var verificandoUbicacion by remember { mutableStateOf(false) }
    var ubicacionVerificadaMensaje by remember { mutableStateOf("") }

    // Selectores
    var categoriaSeleccionada by remember { mutableStateOf("Frutas") }
    var provinciaSeleccionada by remember { mutableStateOf("Imbabura") }
    var emojiSeleccionado by remember { mutableStateOf("🍎") }

    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedProvincia by remember { mutableStateOf(false) }

    var errorMensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val listaEmojis = listOf("🥑", "🍓", "🥕", "🧀", "🍋", "🧅", "🧶", "🏺", "🍯", "🥔", "🍍", "🍌", "🍎", "🥦", "🌽")

    // GPS Services Location
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            if (fineGranted || coarseGranted) {
                // Obtener ubicación
                obtenerUbicacion(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    onStart = {
                        verificandoUbicacion = true
                        errorMensaje = ""
                        ubicacionVerificadaMensaje = ""
                    },
                    onLocationResult = { location ->
                        if (location != null) {
                            val isMock = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                location.isMock
                            } else {
                                @Suppress("DEPRECATION")
                                location.isFromMockProvider
                            }
                            
                            if (isMock) {
                                verificandoUbicacion = false
                                errorMensaje = "🔒 Ubicación rechazada: Se detectó el uso de un GPS simulado/falso (Fraude detectado)."
                                ubicacionEsReal = false
                            } else {
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.geocodingService.reverseGeocode(
                                            lat = location.latitude,
                                            lon = location.longitude
                                        )
                                        val countryCode = response.address?.country_code?.lowercase() ?: ""
                                        val country = response.address?.country?.lowercase() ?: ""
                                        
                                        if (countryCode == "ec" || country.contains("ecuador")) {
                                            latitud = location.latitude
                                            longitud = location.longitude
                                            direccionVerificada = response.display_name
                                            ubicacionEsReal = true
                                            errorMensaje = ""
                                        } else {
                                            errorMensaje = "🔒 Ubicación fuera de rango: El geolocalizador indica que estás en: ${response.address?.country ?: "País Desconocido"}. Solo se permiten productores dentro de Ecuador."
                                            ubicacionEsReal = false
                                        }
                                    } catch (e: Exception) {
                                        errorMensaje = "Error al verificar procedencia de ubicación. Compruebe su conexión a internet. (${e.message})"
                                        ubicacionEsReal = false
                                    } finally {
                                        verificandoUbicacion = false
                                    }
                                }
                            }
                        } else {
                            verificandoUbicacion = false
                            errorMensaje = "No se pudo obtener la ubicación GPS actual. Intente de nuevo con el GPS encendido."
                            ubicacionEsReal = false
                        }
                    }
                )
            } else {
                errorMensaje = "Permiso de ubicación denegado. Se requiere el GPS obligatoriamente para publicar."
            }
        }
    )

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

                    Spacer(modifier = Modifier.height(16.dp))

                    // GPS Obligatorio y Antifraude
                    Text("Ubicación del productor", color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (verificandoUbicacion) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(
                                        color = VerdePrincipal,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text("Validando GPS real y región (Ecuador)...", color = Blanco, fontSize = 12.sp)
                                }
                            } else if (ubicacionEsReal) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🔒", fontSize = 14.sp)
                                    Text(
                                        text = "Ubicación Verificada: Sin simuladores / Ecuador",
                                        color = Color(0xFF66BB6A),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = direccionVerificada,
                                    color = Blanco,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lat: ${String.format("%.5f", latitud)}, Lon: ${String.format("%.5f", longitud)}",
                                    color = GrisTexto,
                                    fontSize = 10.sp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Text(
                                        text = "Ubicación GPS Requerida Obligatoriamente",
                                        color = Color(0xFFFFB300),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Debes capturar tu ubicación geográfica para que los clientes puedan verificar el origen real del producto.",
                                    color = GrisTexto,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                    if (fineGranted || coarseGranted) {
                                        obtenerUbicacion(
                                            context = context,
                                            fusedLocationClient = fusedLocationClient,
                                            onStart = {
                                                verificandoUbicacion = true
                                                errorMensaje = ""
                                                ubicacionVerificadaMensaje = ""
                                            },
                                            onLocationResult = { location ->
                                                if (location != null) {
                                                    val isMock = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                                        location.isMock
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        location.isFromMockProvider
                                                    }
                                                    
                                                    if (isMock) {
                                                        verificandoUbicacion = false
                                                        errorMensaje = "🔒 Ubicación rechazada: Se detectó el uso de un GPS simulado/falso (Fraude detectado)."
                                                        ubicacionEsReal = false
                                                    } else {
                                                        coroutineScope.launch {
                                                            try {
                                                                val response = RetrofitClient.geocodingService.reverseGeocode(
                                                                    lat = location.latitude,
                                                                    lon = location.longitude
                                                                )
                                                                val countryCode = response.address?.country_code?.lowercase() ?: ""
                                                                val country = response.address?.country?.lowercase() ?: ""
                                                                
                                                                if (countryCode == "ec" || country.contains("ecuador")) {
                                                                    latitud = location.latitude
                                                                    longitud = location.longitude
                                                                    direccionVerificada = response.display_name
                                                                    ubicacionEsReal = true
                                                                    errorMensaje = ""
                                                                } else {
                                                                    errorMensaje = "🔒 Ubicación fuera de rango: El geolocalizador indica que estás en: ${response.address?.country ?: "País Desconocido"}. Solo se permiten productores dentro de Ecuador."
                                                                    ubicacionEsReal = false
                                                                }
                                                            } catch (e: Exception) {
                                                                errorMensaje = "Error al verificar procedencia de ubicación. Compruebe su conexión a internet. (${e.message})"
                                                                ubicacionEsReal = false
                                                            } finally {
                                                                verificandoUbicacion = false
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    verificandoUbicacion = false
                                                    errorMensaje = "No se pudo obtener la ubicación GPS actual. Intente de nuevo con el GPS encendido."
                                                    ubicacionEsReal = false
                                                }
                                            }
                                        )
                                    } else {
                                        launcher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (ubicacionEsReal) Color(0xFF2E7D32) else VerdePrincipal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text(
                                        text = if (ubicacionEsReal) "Actualizar Ubicación GPS" else "Obtener y Verificar Ubicación (GPS)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                }
                            }
                        }
                    }

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
                                !ubicacionEsReal -> errorMensaje = "⚠️ Debes verificar tu ubicación GPS obligatoriamente para poder publicar."
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
                                        descripcion = descripcion,
                                        latitud = latitud,
                                        longitud = longitud,
                                        direccionVerificada = direccionVerificada,
                                        ubicacionEsReal = ubicacionEsReal
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
                        enabled = !cargando && !verificandoUbicacion,
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

private fun obtenerUbicacion(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onStart: () -> Unit,
    onLocationResult: (Location?) -> Unit
) {
    onStart()
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                onLocationResult(location)
            }
            .addOnFailureListener {
                onLocationResult(null)
            }
    } else {
        onLocationResult(null)
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
