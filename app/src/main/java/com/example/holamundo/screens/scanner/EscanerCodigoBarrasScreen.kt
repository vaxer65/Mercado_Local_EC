package com.example.holamundo.screens.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.holamundo.Colores
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private val VerdePrincipal = Colores.VerdePrincipal
private val Blanco         = Colores.Blanco

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerCodigoBarrasScreen(
    catalogoViewModel: CatalogoViewModel,
    onProductoEncontrado: (String) -> Unit, // Redirige a detalle con el ID del producto
    onAtrasClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
            Toast.makeText(context, "Se requiere permiso de cámara para escanear", Toast.LENGTH_LONG).show()
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
            // Camera Preview & ML Kit Analyzer
            CameraPreviewWithAnalyzer(
                catalogoViewModel = catalogoViewModel,
                onBarcodeScanned = { barcodeVal ->
                    // Buscar producto en la lista local por codigoBarras o por id
                    val todosProductos = catalogoViewModel.productosFiltrados.value
                    val producto = todosProductos.firstOrNull { 
                        it.codigoBarras == barcodeVal || it.id == barcodeVal 
                    }
                    if (producto != null) {
                        onProductoEncontrado(producto.id)
                    } else {
                        Toast.makeText(context, "Producto no registrado ($barcodeVal)", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Scanning reticle and laser animation
            ScannerOverlay()

            // Header toolbar
            TopAppBar(
                title = {
                    Text(
                        "Escáner de Productos",
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Blanco)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)
                )
            )

            // Bottom instruction card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Apunta al código de barras o QR",
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "El escáner detectará automáticamente el producto.",
                        color = Colores.GrisTexto,
                        fontSize = 12.sp
                    )
                }
            }
        }
    } else {
        // Fallback view when camera permission is denied
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Colores.FondoOscuro),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Cargando escáner...", color = Blanco)
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = VerdePrincipal)
            }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreviewWithAnalyzer(
    catalogoViewModel: CatalogoViewModel,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Control to prevent multiple fast scans of the same item
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableLongStateOf(0L) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Configure Preview UseCase
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                // Configure ML Kit Scanner Options
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODE_128
                    )
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                // Configure Image Analysis UseCase
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val barcode = barcodes.firstOrNull()
                                val rawValue = barcode?.rawValue
                                if (rawValue != null) {
                                    val now = System.currentTimeMillis()
                                    // Debounce scans (2 seconds gap or different code)
                                    if (rawValue != lastScannedCode || now - lastScanTime > 2000L) {
                                        lastScannedCode = rawValue
                                        lastScanTime = now
                                        onBarcodeScanned(rawValue)
                                    }
                                }
                            }
                            .addOnFailureListener {
                                // Failures are ignored to continue scanning
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Toast.makeText(context, "Error al iniciar cámara: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun ScannerOverlay() {
    // Animation for the scanning laser line
    val transition = rememberInfiniteTransition(label = "laser")
    val laserYPercentage by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserLine"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Reticle size (centered square)
        val boxSize = width * 0.7f
        val left = (width - boxSize) / 2f
        val top = (height - boxSize) / 2.2f
        val right = left + boxSize
        val bottom = top + boxSize

        // 1. Draw darkened outer area
        val path = Path().apply {
            addRect(Rect(0f, 0f, width, height))
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            )
        }

        // Clip out the transparent scanner window and paint the rest dark
        clipPath(path, clipOp = ClipOp.Difference) {
            drawRect(
                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
                size = size
            )
        }

        // 2. Draw green scanner borders
        val cornerLen = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val paintColor = VerdePrincipal

        // Top Left
        drawLine(paintColor, Offset(left, top), Offset(left + cornerLen, top), strokeWidth)
        drawLine(paintColor, Offset(left, top), Offset(left, top + cornerLen), strokeWidth)

        // Top Right
        drawLine(paintColor, Offset(right, top), Offset(right - cornerLen, top), strokeWidth)
        drawLine(paintColor, Offset(right, top), Offset(right, top + cornerLen), strokeWidth)

        // Bottom Left
        drawLine(paintColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeWidth)
        drawLine(paintColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeWidth)

        // Bottom Right
        drawLine(paintColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeWidth)
        drawLine(paintColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeWidth)

        // 3. Draw animated laser line
        val laserY = top + (boxSize * laserYPercentage)
        drawLine(
            color = VerdePrincipal,
            start = Offset(left + 10f, laserY),
            end = Offset(right - 10f, laserY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
