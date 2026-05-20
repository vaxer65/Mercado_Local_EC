package com.example.holamundo.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.holamundo.viewmodel.AuthViewModel    

val VerdePrincipal = Color(0xFF1D9E75)
val VerdeOscuro    = Color(0xFF0F6E56)
val FondoOscuro    = Color(0xFF1A1A2E)
val FondoTarjeta   = Color(0xFF2A2A3E)
val GrisTexto      = Color(0xFF9E9E9E)
val Blanco         = Color(0xFFFFFFFF)
val Rojo           = Color(0xFFE24B4A)

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onVolverAlHome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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

            Spacer(modifier = Modifier.height(32.dp))

            // Logo y título
            Text(text = "🌿", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "MercadoLocal EC",
                color = VerdePrincipal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Inicia sesión en tu cuenta",
                color = GrisTexto,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Tarjeta del formulario
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Campo email
                    Text(
                        text = "Correo electrónico",
                        color = GrisTexto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMensaje = ""
                        },
                        placeholder = { Text("usuario@correo.com", color = GrisTexto) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = GrisTexto)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF3A3A5E),
                            focusedBorderColor   = VerdePrincipal,
                            unfocusedContainerColor = Color(0xFF1A1A2E),
                            focusedContainerColor   = Color(0xFF1A1A2E),
                            unfocusedTextColor = Blanco,
                            focusedTextColor   = Blanco
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo contraseña
                    Text(
                        text = "Contraseña",
                        color = GrisTexto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMensaje = ""
                        },
                        placeholder = { Text("Mínimo 6 caracteres", color = GrisTexto) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = GrisTexto)
                        },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF3A3A5E),
                            focusedBorderColor   = VerdePrincipal,
                            unfocusedContainerColor = Color(0xFF1A1A2E),
                            focusedContainerColor   = Color(0xFF1A1A2E),
                            unfocusedTextColor = Blanco,
                            focusedTextColor   = Blanco
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // ¿Olvidaste contraseña?
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = VerdePrincipal,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* próximamente */ }
                    )

                    // Mensaje de error
                    if (errorMensaje.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMensaje,
                            color = Rojo,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón ingresar
                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> errorMensaje = "Ingresa tu correo electrónico"
                                password.length < 6 -> errorMensaje = "La contraseña debe tener al menos 6 caracteres"
                                else -> {
                                    cargando = true
                                    errorMensaje = ""
                                    authViewModel.iniciarSesion(email, password) { exito, error ->
                                        cargando = false
                                        if (exito) {
                                            onLoginExitoso()
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
                            CircularProgressIndicator(
                                color = Blanco,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Ingresar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blanco
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ir a registro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = GrisTexto,
                    fontSize = 14.sp
                )
                Text(
                    text = "Regístrate gratis",
                    color = VerdePrincipal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onIrARegistro() }
                )
            }
        }
    }
}