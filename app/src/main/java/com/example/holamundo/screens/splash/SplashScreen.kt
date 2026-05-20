package com.example.holamundo.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

val VerdePrincipal = Color(0xFF1D9E75)
val VerdeSuave     = Color(0xFF9FE1CB)

@Composable
fun SplashScreen(onSplashTerminado: () -> Unit) {

    // Controla la animación de aparición
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha_splash"
    )

    // Espera 2.5 segundos y navega
    LaunchedEffect(Unit) {
        visible = true
        delay(2500)
        onSplashTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdePrincipal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alpha)
        ) {
            Text(
                text = "🌿",
                fontSize = 72.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MercadoLocal EC",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Conectamos productores con tu mesa",
                color = VerdeSuave,
                fontSize = 14.sp
            )
        }
    }
}