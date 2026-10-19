package com.example.holamundo.screens.ar

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.holamundo.Colores
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener

private val VerdePrincipal = Colores.VerdePrincipal
private val Blanco         = Colores.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizadorARScreen(
    productoId: String,
    catalogoViewModel: CatalogoViewModel,
    onAtrasClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Buscar el producto para mostrar su nombre
    val producto = remember(productoId) {
        catalogoViewModel.productosFiltrados.value.firstOrNull { it.id == productoId }
    }
    val productoNombre = producto?.nombre ?: "Producto 3D"

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
        if (!concedido) {
            Toast.makeText(context, "Se requiere permiso de cámara para usar Realidad Aumentada", Toast.LENGTH_LONG).show()
            onAtrasClick()
        }
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            launcherPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    if (tienePermisoCamara) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // --- SCENEVIEW AR CORE VIEWER ---
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)
            val childNodes = rememberNodes()
            val cameraNode = rememberARCameraNode(engine)

            var currentFrame by remember { mutableStateOf<Frame?>(null) }

            // Selección dinámica del modelo 3D según el producto o categoría
            val modelUrl = remember(producto) {
                val nombre = producto?.nombre ?: ""
                val categoria = producto?.categoria ?: ""
                when {
                    nombre.contains("Aguacate", ignoreCase = true) ->
                        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Avocado/glTF-Binary/Avocado.glb"
                    nombre.contains("Fresa", ignoreCase = true) ->
                        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Duck/glTF-Binary/Duck.glb" // Pato representativo de finca
                    categoria == "Lácteos" ->
                        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/WaterBottle/glTF-Binary/WaterBottle.glb" // Botella de leche/líquidos
                    categoria == "Verduras" ->
                        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Fox/glTF-Binary/Fox.glb" // Zorro representativo del campo
                    else ->
                        "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Box/glTF-Binary/Box.glb" // Caja de mercadería
                }
            }

            ARScene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                childNodes = childNodes,
                planeRenderer = true, // Muestra los puntitos de detección del plano
                onSessionUpdated = { _, frame ->
                    currentFrame = frame
                },
                onGestureListener = rememberOnGestureListener(
                    onSingleTapConfirmed = { motionEvent, node ->
                        val frame = currentFrame
                        if (frame != null && frame.camera.trackingState == TrackingState.TRACKING) {
                            // Detectar toques en plano horizontal/vertical
                            val hitResults = frame.hitTest(motionEvent.x, motionEvent.y)
                            val hitResult = hitResults.firstOrNull { hit ->
                                val trackable = hit.trackable
                                trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)
                            }
                            val anchor = hitResult?.createAnchor()

                            if (anchor != null) {
                                // Crear un nodo ancla en la posición del plano
                                val anchorNode = AnchorNode(engine = engine, anchor = anchor)
                                
                                // Cargar el modelo 3D (Aguacate orgánico) sobre el ancla
                                try {
                                    val modelInstance = modelLoader.createModelInstance(modelUrl)
                                    if (modelInstance != null) {
                                        val modelNode = ModelNode(
                                            modelInstance = modelInstance,
                                            scaleToUnits = 0.5f // Redimensionar a un tamaño realista (aprox. 50cm)
                                        ).apply {
                                            isEditable = true // Permite rotar/escalar con pellizcos
                                        }
                                        anchorNode.addChildNode(modelNode)
                                        childNodes.clear() // Limpiar stands anteriores
                                        childNodes.add(anchorNode)
                                    } else {
                                        Toast.makeText(context, "Cargando modelo 3D desde la nube...", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al cargar modelo 3D: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            )

            // --- HEADER TOOLBAR ---
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = productoNombre,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Visualizador de Stand AR",
                            color = Color(0xFF9FE1CB),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            )

            // --- INSTRUCTIONS OVERLAY ---
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1. Mueve el celular despacio para detectar el suelo o mesa",
                        color = Blanco,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Toca los puntos blancos para colocar el producto 3D",
                        color = Blanco,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. Usa gestos para rotar o cambiar su tamaño",
                        color = Colores.GrisTexto,
                        fontSize = 11.sp
                    )
                }
            }
        }
    } else {
        // Fallback UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Colores.FondoOscuro),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Iniciando Realidad Aumentada...", color = Blanco)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = VerdePrincipal)
            }
        }
    }
}
