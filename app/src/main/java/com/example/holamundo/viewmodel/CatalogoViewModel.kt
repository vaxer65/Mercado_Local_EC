package com.example.holamundo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.holamundo.model.Producto
import com.example.holamundo.model.Valoracion
import com.example.holamundo.model.room.AppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    private val productoDao = AppDatabase.getDatabase(application).productoDao()
    private val valoracionDao = AppDatabase.getDatabase(application).valoracionDao()

    // Filtros activos
    private val _categoriaActiva = MutableStateFlow("Todos")
    val categoriaActiva: StateFlow<String> = _categoriaActiva.asStateFlow()

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Producto seleccionado para ver detalle
    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado.asStateFlow()

    // Valoraciones del producto seleccionado
    private val _valoraciones = MutableStateFlow<List<Valoracion>>(emptyList())
    val valoraciones: StateFlow<List<Valoracion>> = _valoraciones.asStateFlow()

    private var jobValoraciones: kotlinx.coroutines.Job? = null

    // Todos los productos de la DB
    private val _todosLosProductos = MutableStateFlow<List<Producto>>(emptyList())

    // Productos filtrados (resultado final)
    private val _productosFiltrados = MutableStateFlow<List<Producto>>(emptyList())
    val productosFiltrados: StateFlow<List<Producto>> = _productosFiltrados.asStateFlow()

    init {
        // Observar la lista de productos de Room en tiempo real
        viewModelScope.launch {
            productoDao.obtenerTodos().collect { lista ->
                _todosLosProductos.value = lista
                aplicarFiltros()
            }
        }
    }

    fun cambiarCategoria(categoria: String) {
        _categoriaActiva.value = categoria
        aplicarFiltros()
    }

    fun cambiarBusqueda(texto: String) {
        _busqueda.value = texto
        aplicarFiltros()
    }

    fun seleccionarProducto(producto: Producto) {
        _productoSeleccionado.value = producto
        jobValoraciones?.cancel()
        jobValoraciones = viewModelScope.launch {
            valoracionDao.obtenerPorProducto(producto.id).collect { lista ->
                _valoraciones.value = lista
            }
        }
    }

    fun limpiarProductoSeleccionado() {
        _productoSeleccionado.value = null
        jobValoraciones?.cancel()
        _valoraciones.value = emptyList()
    }

    fun agregarProducto(
        nombre: String,
        productorNombre: String,
        productorId: String,
        precio: Double,
        unidad: String,
        categoria: String,
        stock: Int,
        provincia: String,
        emoji: String,
        descripcion: String,
        latitud: Double,
        longitud: Double,
        direccionVerificada: String,
        ubicacionEsReal: Boolean,
        onCompletado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val nuevoId = java.util.UUID.randomUUID().toString()
            val nuevoProducto = Producto(
                id = nuevoId,
                nombre = nombre,
                productor = productorNombre,
                productorId = productorId,
                precio = precio,
                unidad = unidad,
                categoria = categoria,
                stock = stock,
                provincia = provincia,
                emoji = emoji,
                descripcion = descripcion,
                imagenUrl = "",
                latitud = latitud,
                longitud = longitud,
                direccionVerificada = direccionVerificada,
                ubicacionEsReal = ubicacionEsReal
            )
            productoDao.insertar(nuevoProducto)
            onCompletado(true)
        }
    }

    private fun aplicarFiltros() {
        var resultado = _todosLosProductos.value

        // Filtrar por categoría
        if (_categoriaActiva.value != "Todos") {
            resultado = resultado.filter { it.categoria == _categoriaActiva.value }
        }

        // Filtrar por búsqueda
        if (_busqueda.value.isNotBlank()) {
            resultado = resultado.filter {
                it.nombre.contains(_busqueda.value, ignoreCase = true) ||
                        it.productor.contains(_busqueda.value, ignoreCase = true) ||
                        it.provincia.contains(_busqueda.value, ignoreCase = true)
            }
        }

        _productosFiltrados.value = resultado
    }

    fun agregarValoracion(
        productoId: String,
        compradorId: String,
        compradorNombre: String,
        calificacion: Int,
        comentario: String,
        onCompletado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val nuevaValoracion = Valoracion(
                productoId = productoId,
                compradorId = compradorId,
                compradorNombre = compradorNombre,
                calificacion = calificacion,
                comentario = comentario,
                timestamp = System.currentTimeMillis()
            )
            valoracionDao.insertar(nuevaValoracion)
            onCompletado(true)
        }
    }
}