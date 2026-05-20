package com.example.holamundo.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.model.RolUsuario
import com.example.holamundo.viewmodel.AuthViewModel

@Composable
fun RegistroScreen(
    authViewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    onVolverAlHome: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.CONSUMIDOR) }
    var terminosAceptados by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            // Botón volver
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Volver",
                    color = GrisTexto,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onVolverAlHome() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(text = "🌿", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crear cuenta",
                color = Blanco,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Únete a MercadoLocal EC",
                color = GrisTexto,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de rol
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Soy...",
                        color = GrisTexto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chip Consumidor
                        Button(
                            onClick = { rolSeleccionado = RolUsuario.CONSUMIDOR },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rolSeleccionado == RolUsuario.CONSUMIDOR)
                                    VerdePrincipal else Color(0xFF1A1A2E)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(
                                text = "🛒 Consumidor",
                                fontSize = 12.sp,
                                color = Blanco,
                                fontWeight = if (rolSeleccionado == RolUsuario.CONSUMIDOR)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        // Chip Productor
                        Button(
                            onClick = { rolSeleccionado = RolUsuario.PRODUCTOR },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rolSeleccionado == RolUsuario.PRODUCTOR)
                                    VerdePrincipal else Color(0xFF1A1A2E)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Text(
                                text = "🌱 Productor",
                                fontSize = 12.sp,
                                color = Blanco,
                                fontWeight = if (rolSeleccionado == RolUsuario.PRODUCTOR)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Formulario
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Nombre
                    CampoTexto(
                        label = "Nombre completo",
                        valor = nombre,
                        placeholder = "Ej: Ana Torres",
                        icono = { Icon(Icons.Default.Person, null, tint = GrisTexto) },
                        onCambio = { nombre = it; errorMensaje = "" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email
                    CampoTexto(
                        label = "Correo electrónico",
                        valor = email,
                        placeholder = "ana@correo.com",
                        icono = { Icon(Icons.Default.Email, null, tint = GrisTexto) },
                        teclado = KeyboardType.Email,
                        onCambio = { email = it; errorMensaje = "" }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contraseña
                    Text(
                        text = "Contraseña",
                        color = GrisTexto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMensaje = "" },
                        placeholder = { Text("Mínimo 6 caracteres", color = GrisTexto) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = GrisTexto) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = GrisTexto
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = campColores(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Términos y condiciones
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = terminosAceptados,
                            onCheckedChange = { terminosAceptados = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = VerdePrincipal,
                                uncheckedColor = GrisTexto
                            )
                        )
                        Text(
                            text = "Acepto los ",
                            color = GrisTexto,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "términos y condiciones",
                            color = VerdePrincipal,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { }
                        )
                    }

                    // Error
                    if (errorMensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMensaje,
                            color = Rojo,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón crear cuenta
                    Button(
                        onClick = {
                            when {
                                nombre.isBlank() -> errorMensaje = "Ingresa tu nombre completo"
                                email.isBlank() -> errorMensaje = "Ingresa tu correo electrónico"
                                !email.contains("@") -> errorMensaje = "El correo no es válido"
                                password.length < 6 -> errorMensaje = "La contraseña debe tener al menos 6 caracteres"
                                !terminosAceptados -> errorMensaje = "Debes aceptar los términos y condiciones"
                                else -> {
                                    cargando = true
                                    errorMensaje = ""
                                    authViewModel.registrarUsuario(nombre, email, password, rolSeleccionado) { exito, error ->
                                        cargando = false
                                        if (exito) {
                                            onRegistroExitoso()
                                        } else {
                                            errorMensaje = error
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
                            Text("Crear mi cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blanco)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ya tienes cuenta
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "¿Ya tienes cuenta? ", color = GrisTexto, fontSize = 14.sp)
                Text(
                    text = "Iniciar sesión",
                    color = VerdePrincipal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onIrALogin() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Componente reutilizable para campos de texto
@Composable
fun CampoTexto(
    label: String,
    valor: String,
    placeholder: String,
    icono: @Composable () -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    Text(text = label, color = GrisTexto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(placeholder, color = GrisTexto) },
        leadingIcon = icono,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        colors = campColores(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

// Colores reutilizables para los campos
@Composable
fun campColores() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor    = Color(0xFF3A3A5E),
    focusedBorderColor      = VerdePrincipal,
    unfocusedContainerColor = Color(0xFF1A1A2E),
    focusedContainerColor   = Color(0xFF1A1A2E),
    unfocusedTextColor      = Blanco,
    focusedTextColor        = Blanco
)