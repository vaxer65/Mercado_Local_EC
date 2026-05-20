package com.example.holamundo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.holamundo.PantallaInicio
import com.example.holamundo.screens.auth.LoginScreen
import com.example.holamundo.screens.auth.RegistroScreen
import com.example.holamundo.screens.home.CatalogoScreen
import com.example.holamundo.screens.home.DetalleProductoScreen
import com.example.holamundo.screens.home.PedidosScreen
import com.example.holamundo.screens.home.ChatScreen
import com.example.holamundo.screens.productor.PanelProductorScreen
import com.example.holamundo.screens.productor.AgregarProductoScreen
import com.example.holamundo.screens.splash.SplashScreen
import com.example.holamundo.viewmodel.AuthViewModel
import com.example.holamundo.viewmodel.CatalogoViewModel
import com.example.holamundo.viewmodel.PedidosViewModel
import com.example.holamundo.viewmodel.ChatViewModel

@Composable
fun NavegacionPrincipal() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val catalogoViewModel: CatalogoViewModel = viewModel()
    val pedidosViewModel: PedidosViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Rutas.Splash.ruta
    ) {

        composable(Rutas.Splash.ruta) {
            SplashScreen(
                onSplashTerminado = {
                    navController.navigate(Rutas.Home.ruta) {
                        popUpTo(Rutas.Splash.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.Home.ruta) {
            PantallaInicio(
                authViewModel = authViewModel,
                catalogoViewModel = catalogoViewModel,
                onIrALogin = { navController.navigate(Rutas.Login.ruta) },
                onIrACatalogo = { navController.navigate(Rutas.Catalogo.ruta) },
                onIrAPedidos = { navController.navigate(Rutas.MisPedidos.ruta) },
                onIrAPerfil = { /* Manejado internamente por diálogo */ },
                onProductoClick = { producto ->
                    catalogoViewModel.seleccionarProducto(producto)
                    navController.navigate(Rutas.Detalle.crearRuta(producto.id))
                }
            )
        }

        composable(Rutas.Catalogo.ruta) {
            CatalogoScreen(
                catalogoViewModel = catalogoViewModel,
                onProductoClick = { producto ->
                    catalogoViewModel.seleccionarProducto(producto)
                    navController.navigate(Rutas.Detalle.crearRuta(producto.id))
                },
                onVolverAlHome = { navController.popBackStack() }
            )
        }

        composable(Rutas.Detalle.ruta) {
            val producto = catalogoViewModel.productoSeleccionado.collectAsState().value
            producto?.let { prod ->
                DetalleProductoScreen(
                    producto = prod,
                    authViewModel = authViewModel,
                    onVolver = { navController.popBackStack() },
                    onIrALogin = { navController.navigate(Rutas.Login.ruta) },
                    onHacerPedido = { _, cantidad ->
                        val consumidorId = authViewModel.usuarioActualUid
                        if (consumidorId != null) {
                            pedidosViewModel.realizarPedido(prod.id, consumidorId, cantidad) { exito, _ ->
                                if (exito) {
                                    navController.navigate(Rutas.MisPedidos.ruta) {
                                        popUpTo(Rutas.Home.ruta) { inclusive = false }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        composable(Rutas.Login.ruta) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginExitoso = {
                    val destino = if (authViewModel.esProductor)
                        Rutas.PanelProductor.ruta else Rutas.HomeCompleto.ruta
                    navController.navigate(destino) {
                        popUpTo(Rutas.Home.ruta) { inclusive = false }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.Registro.ruta) },
                onVolverAlHome = { navController.popBackStack() }
            )
        }

        composable(Rutas.Registro.ruta) {
            RegistroScreen(
                authViewModel = authViewModel,
                onRegistroExitoso = {
                    val destino = if (authViewModel.esProductor)
                        Rutas.PanelProductor.ruta else Rutas.HomeCompleto.ruta
                    navController.navigate(destino) {
                        popUpTo(Rutas.Home.ruta) { inclusive = false }
                    }
                },
                onIrALogin = { navController.navigate(Rutas.Login.ruta) },
                onVolverAlHome = { navController.popBackStack() }
            )
        }

        composable(Rutas.HomeCompleto.ruta) {
            PantallaInicio(
                authViewModel = authViewModel,
                catalogoViewModel = catalogoViewModel,
                onIrALogin = {},
                onIrACatalogo = { navController.navigate(Rutas.Catalogo.ruta) },
                onIrAPedidos = { navController.navigate(Rutas.MisPedidos.ruta) },
                onIrAPerfil = { /* Manejado internamente por diálogo */ },
                onProductoClick = { producto ->
                    catalogoViewModel.seleccionarProducto(producto)
                    navController.navigate(Rutas.Detalle.crearRuta(producto.id))
                }
            )
        }

        composable(Rutas.MisPedidos.ruta) {
            PedidosScreen(
                authViewModel = authViewModel,
                pedidosViewModel = pedidosViewModel,
                onVolverAlHome = { navController.navigate(Rutas.Home.ruta) { popUpTo(Rutas.Home.ruta) { inclusive = true } } },
                onIrACatalogo = { navController.navigate(Rutas.Catalogo.ruta) },
                onContactarProductor = { prodId, _ ->
                    val consumidorId = authViewModel.usuarioActualUid ?: ""
                    val chatId = "${consumidorId}_${prodId}"
                    navController.navigate(Rutas.Chat.crearRuta(chatId))
                }
            )
        }

        composable(Rutas.Chat.ruta) { navBackStackEntry ->
            val chatId = navBackStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                authViewModel = authViewModel,
                chatViewModel = chatViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PanelProductor.ruta) {
            PanelProductorScreen(
                authViewModel = authViewModel,
                catalogoViewModel = catalogoViewModel,
                pedidosViewModel = pedidosViewModel,
                onIrAAgregarProducto = { navController.navigate(Rutas.AgregarProducto.ruta) },
                onIrAlChat = { chatId -> navController.navigate(Rutas.Chat.crearRuta(chatId)) },
                onCerrarSesionExito = {
                    navController.navigate(Rutas.Home.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.AgregarProducto.ruta) {
            AgregarProductoScreen(
                authViewModel = authViewModel,
                catalogoViewModel = catalogoViewModel,
                onVolver = { navController.popBackStack() },
                onAgregarExito = { navController.popBackStack() }
            )
        }
    }
}