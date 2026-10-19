package com.example.holamundo.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.holamundo.R
import kotlinx.coroutines.delay

private val VerdeSuave = Color(0xFF9FE1CB)

@Composable
fun SplashScreen(onSplashTerminado: () -> Unit) {

    // Controla la animación de aparición y zoom
    var visible by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "alpha_splash"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.75f,
        animationSpec = tween(durationMillis = 1200),
        label = "scale_splash"
    )

    // Espera 2.8 segundos y navega para dar tiempo a apreciar la animación
    LaunchedEffect(Unit) {
        visible = true
        delay(2800)
        onSplashTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF0F6E56))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Tarjeta de Banner Animada
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(280.dp)
                    .height(170.dp)
                    .graphicsLayer(
                        alpha = alpha,
                        scaleX = scale,
                        scaleY = scale
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.market_banner),
                    contentDescription = "Mercado Local Ecuador",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Texto del título con animación de entrada
            Text(
                text = "MercadoLocal EC",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtítulo con animación de entrada
            Text(
                text = "Conectamos productores con tu mesa",
                color = VerdeSuave,
                fontSize = 14.sp,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}