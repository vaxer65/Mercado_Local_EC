package com.example.holamundo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.holamundo.model.EstadoAuth
import com.example.holamundo.model.RolUsuario
import com.example.holamundo.model.Usuario
import com.example.holamundo.model.room.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getDatabase(application).usuarioDao()

    // Estado global de autenticación
    private val _estadoAuth = MutableStateFlow<EstadoAuth>(EstadoAuth.Visitante)
    val estadoAuth: StateFlow<EstadoAuth> = _estadoAuth.asStateFlow()

    // Verifica si el usuario está logueado
    val estaLogueado: Boolean
        get() = _estadoAuth.value is EstadoAuth.Logueado

    // Obtiene el UID del usuario logueado
    val usuarioActualUid: String?
        get() = (_estadoAuth.value as? EstadoAuth.Logueado)?.uid

    // Obtiene el nombre del usuario logueado
    val usuarioActualNombre: String?
        get() = (_estadoAuth.value as? EstadoAuth.Logueado)?.nombre

    // Verifica el rol del usuario
    val esProductor: Boolean
        get() = (_estadoAuth.value as? EstadoAuth.Logueado)?.rol == RolUsuario.PRODUCTOR

    val esConsumidor: Boolean
        get() = (_estadoAuth.value as? EstadoAuth.Logueado)?.rol == RolUsuario.CONSUMIDOR

    // Cerrar sesión
    fun cerrarSesion() {
        _estadoAuth.value = EstadoAuth.Visitante
    }

    // Iniciar sesión en la base de datos local
    fun iniciarSesion(email: String, clave: String, onResultado: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val usuario = usuarioDao.obtenerPorEmail(email)
            if (usuario != null) {
                if (usuario.password == clave) {
                    val rolEnum = if (usuario.rol == "PRODUCTOR") RolUsuario.PRODUCTOR else RolUsuario.CONSUMIDOR
                    _estadoAuth.value = EstadoAuth.Logueado(
                        uid = usuario.uid,
                        nombre = usuario.nombre,
                        email = usuario.email,
                        rol = rolEnum
                    )
                    onResultado(true, "")
                } else {
                    onResultado(false, "Contraseña incorrecta")
                }
            } else {
                onResultado(false, "El usuario no existe en la base de datos")
            }
        }
    }

    // Registrar un nuevo usuario en la base de datos local
    fun registrarUsuario(
        nombre: String,
        email: String,
        clave: String,
        rol: RolUsuario,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val usuarioExistente = usuarioDao.obtenerPorEmail(email)
            if (usuarioExistente != null) {
                onResultado(false, "El correo electrónico ya está registrado")
            } else {
                val nuevoUid = UUID.randomUUID().toString()
                val nuevoUsuario = Usuario(
                    uid = nuevoUid,
                    nombre = nombre,
                    email = email,
                    password = clave,
                    rol = rol.name
                )
                usuarioDao.registrar(nuevoUsuario)
                _estadoAuth.value = EstadoAuth.Logueado(
                    uid = nuevoUid,
                    nombre = nombre,
                    email = email,
                    rol = rol
                )
                onResultado(true, "")
            }
        }
    }
}