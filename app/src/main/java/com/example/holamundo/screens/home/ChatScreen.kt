package com.example.holamundo.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.model.Mensaje
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.holamundo.Colores

private val FondoOscuro  = Colores.FondoOscuro
private val FondoTarjeta = Colores.FondoTarjeta
private val VerdePrincipal = Colores.VerdePrincipal
private val Blanco       = Colores.Blanco
private val GrisTexto    = Colores.GrisTexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    onVolver: () -> Unit
) {
    val emisorId = authViewModel.usuarioActualUid ?: ""
    val mensajes by chatViewModel.mensajes.collectAsState()
    var textoMensaje by remember { mutableStateOf("") }

    val listState = rememberLazyListState()

    // Cargar y escuchar mensajes
    LaunchedEffect(chatId) {
        chatViewModel.cargarMensajes(chatId)
    }

    // Auto-scroll al último mensaje cuando llegue uno nuevo
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat de Negociación", color = Blanco, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Canal seguro de MercadoLocal", color = Color(0xFF9FE1CB), fontSize = 12.sp)
                    }
                },
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
        ) {
            // Historial de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mensajes) { mensaje ->
                    val esPropio = mensaje.emisorId == emisorId
                    FilaMensaje(mensaje = mensaje, esPropio = esPropio)
                }
            }

            // Barra inferior para enviar mensaje
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FondoTarjeta)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoMensaje,
                    onValueChange = { textoMensaje = it },
                    placeholder = { Text("Escribe un mensaje...", color = GrisTexto) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF3A3A5E),
                        focusedBorderColor = VerdePrincipal,
                        unfocusedContainerColor = FondoOscuro,
                        focusedContainerColor = FondoOscuro,
                        unfocusedTextColor = Blanco,
                        focusedTextColor = Blanco
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (textoMensaje.isNotBlank()) {
                            chatViewModel.enviarMensaje(chatId, emisorId, textoMensaje)
                            textoMensaje = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Blanco
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilaMensaje(mensaje: Mensaje, esPropio: Boolean) {
    val alineacion = if (esPropio) Alignment.End else Alignment.Start
    val colorBurbuja = if (esPropio) VerdePrincipal else Color(0xFF3A3A5E)
    val colorTexto = Blanco
    val roundedCorners = if (esPropio) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val hora = formatoHora.format(Date(mensaje.timestamp))

    Column(
        horizontalAlignment = alineacion,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(roundedCorners)
                .background(colorBurbuja)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(
                    text = mensaje.texto,
                    color = colorTexto,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hora,
                    color = if (esPropio) Color(0xFFD1F2E5) else GrisTexto,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
