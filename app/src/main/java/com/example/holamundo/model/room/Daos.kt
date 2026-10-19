package com.example.holamundo.model.room

import androidx.room.*
import com.example.holamundo.model.Mensaje
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.Producto
import com.example.holamundo.model.Usuario
import com.example.holamundo.model.Valoracion
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE uid = :uid LIMIT 1")
    suspend fun obtenerPorId(uid: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registrar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM productos")
    fun obtenerTodos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): Producto?

    @Query("SELECT * FROM productos WHERE productorId = :productorId")
    fun obtenerPorProductor(productorId: String): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vararg productos: Producto)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Query("UPDATE productos SET stock = :nuevoStock WHERE id = :id")
    suspend fun actualizarStock(id: String, nuevoStock: Int)
}

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos WHERE consumidorId = :consumidorId ORDER BY id DESC")
    fun obtenerPorConsumidor(consumidorId: String): Flow<List<Pedido>>

    @Query("SELECT * FROM pedidos WHERE productorId = :productorId ORDER BY id DESC")
    fun obtenerPorProductor(productorId: String): Flow<List<Pedido>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pedido: Pedido)

    @Query("UPDATE pedidos SET estado = :nuevoEstado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, nuevoEstado: String)
}

@Dao
interface MensajeDao {
    @Query("SELECT * FROM mensajes WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun obtenerMensajesChat(chatId: String): Flow<List<Mensaje>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(mensaje: Mensaje)

    @Query("SELECT DISTINCT chatId FROM mensajes WHERE chatId LIKE :userId || '_%' OR chatId LIKE '%_' || :userId")
    fun obtenerChatsDeUsuario(userId: String): Flow<List<String>>
}

@Dao
interface ValoracionDao {
    @Query("SELECT * FROM valoraciones WHERE productoId = :productoId ORDER BY timestamp DESC")
    fun obtenerPorProducto(productoId: String): Flow<List<Valoracion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(valoracion: Valoracion)

    @Query("SELECT AVG(calificacion) FROM valoraciones WHERE productoId = :productoId")
    fun obtenerCalificacionPromedio(productoId: String): Flow<Float?>
}
