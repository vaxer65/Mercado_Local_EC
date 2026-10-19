package com.example.holamundo.model.room

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.holamundo.model.DatosPrueba
import com.example.holamundo.model.Mensaje
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.Producto
import com.example.holamundo.model.Usuario
import com.example.holamundo.model.Valoracion

@Database(entities = [Usuario::class, Producto::class, Pedido::class, Mensaje::class, Valoracion::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun valoracionDao(): ValoracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mercadolocal_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(PrepopulateCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            var count = 0
            try {
                val cursor: Cursor = db.query("SELECT COUNT(*) FROM usuarios", emptyArray<Any?>())
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0)
                }
                cursor.close()
            } catch (e: Exception) {
                count = 0
            }
            if (count == 0) {
                prepopulate(db)
            }
        }

        private fun prepopulate(db: SupportSQLiteDatabase) {
            // Insert default users
            val clientCV = ContentValues().apply {
                put("uid", "cliente_123")
                put("nombre", "Cliente de Prueba")
                put("email", "cliente@test.com")
                put("password", "123456")
                put("rol", "CONSUMIDOR")
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_REPLACE, clientCV)

            val userCV = ContentValues().apply {
                put("uid", "usuario_123")
                put("nombre", "Usuario de Prueba")
                put("email", "usuario@test.com")
                put("password", "123456")
                put("rol", "CONSUMIDOR")
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_REPLACE, userCV)
            
            val producerCV = ContentValues().apply {
                put("uid", "productor_123")
                put("nombre", "Productor de Prueba")
                put("email", "productor@test.com")
                put("password", "123456")
                put("rol", "PRODUCTOR")
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_REPLACE, producerCV)

            val producer2CV = ContentValues().apply {
                put("uid", "productor_456")
                put("nombre", "Lácteos San Antonio")
                put("email", "productor2@test.com")
                put("password", "123456")
                put("rol", "PRODUCTOR")
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_REPLACE, producer2CV)
            
            val producer3CV = ContentValues().apply {
                put("uid", "productor_789")
                put("nombre", "Huerto Orgánico Sierralinda")
                put("email", "productor3@test.com")
                put("password", "123456")
                put("rol", "PRODUCTOR")
            }
            db.insert("usuarios", SQLiteDatabase.CONFLICT_REPLACE, producer3CV)
            
            // Insert default products mapped to our default producers
            DatosPrueba.productos.forEach { prod ->
                val (pId, pName) = when (prod.categoria) {
                    "Lácteos" -> Pair("productor_456", "Lácteos San Antonio")
                    "Verduras" -> Pair("productor_789", "Huerto Orgánico Sierralinda")
                    else -> Pair("productor_123", prod.productor)
                }

                val (lat, lon, addr) = when (prod.id) {
                    "1" -> Triple(-4.00789, -79.21123, "Finca El Paraíso, Loja, Ecuador")
                    "2" -> Triple(-1.24908, -78.61675, "Huerto Don Luis, Ambato, Tungurahua, Ecuador")
                    "3" -> Triple(-1.60155, -79.00222, "Don Rodrigo, Guaranda, Bolívar, Ecuador")
                    "4" -> Triple(-1.67435, -78.64831, "La Quesera, Riobamba, Chimborazo, Ecuador")
                    "5" -> Triple(-1.05458, -80.45445, "Finca El Sol, Portoviejo, Manabí, Ecuador")
                    "6" -> Triple(0.33857, -78.12198, "Agro San Pablo, Otavalo, Imbabura, Ecuador")
                    "7" -> Triple(0.22857, -78.26198, "Taller Mamá Rosa, Otavalo, Imbabura, Ecuador")
                    "8" -> Triple(0.23857, -78.25198, "Taller Otavalo, Otavalo, Imbabura, Ecuador")
                    "9" -> Triple(-0.22985, -78.52495, "Apiario Los Andes, Quito, Pichincha, Ecuador")
                    "10" -> Triple(0.81189, -77.71723, "Familia Chuqui, Tulcán, Carchi, Ecuador")
                    else -> Triple(-0.22985, -78.52495, "Finca Local, Ecuador")
                }

                val prodCV = ContentValues().apply {
                    put("id", prod.id)
                    put("nombre", prod.nombre)
                    put("productor", pName)
                    put("precio", prod.precio)
                    put("unidad", prod.unidad)
                    put("descripcion", prod.descripcion)
                    put("categoria", prod.categoria)
                    put("emoji", prod.emoji)
                    put("stock", prod.stock)
                    put("provincia", prod.provincia)
                    put("imagenUrl", prod.imagenUrl)
                    put("productorId", pId)
                    put("latitud", lat)
                    put("longitud", lon)
                    put("direccionVerificada", addr)
                    put("ubicacionEsReal", true)
                    put("codigoBarras", "786100123456" + prod.id)
                }
                db.insert("productos", SQLiteDatabase.CONFLICT_REPLACE, prodCV)
            }
            
            // Insert default ratings/comments
            val rating1 = ContentValues().apply {
                put("productoId", "1")
                put("compradorId", "cliente_123")
                put("compradorNombre", "Cliente de Prueba")
                put("calificacion", 5)
                put("comentario", "¡Los mejores aguacates que he comprado! Súper cremosos y frescos.")
                put("timestamp", System.currentTimeMillis() - 86400000L)
            }
            db.insert("valoraciones", SQLiteDatabase.CONFLICT_REPLACE, rating1)
            
            val rating2 = ContentValues().apply {
                put("productoId", "1")
                put("compradorId", "cliente_test_2")
                put("compradorNombre", "María José")
                put("calificacion", 4)
                put("comentario", "Muy buenos aguacates. Tardaron un poquito en madurar pero valió la pena.")
                put("timestamp", System.currentTimeMillis() - 172800000L)
            }
            db.insert("valoraciones", SQLiteDatabase.CONFLICT_REPLACE, rating2)
            
            val rating3 = ContentValues().apply {
                put("productoId", "2")
                put("compradorId", "cliente_test_3")
                put("compradorNombre", "Juan Pérez")
                put("calificacion", 5)
                put("comentario", "Las fresas están deliciosas y muy dulces. Recomendadísimas.")
                put("timestamp", System.currentTimeMillis() - 43200000L)
            }
            db.insert("valoraciones", SQLiteDatabase.CONFLICT_REPLACE, rating3)

            // Insert default orders (pedidos) to show that cliente@test.com ordered from the producers
            val order1 = ContentValues().apply {
                put("productoId", "1")
                put("productoNombre", "Aguacate orgánico")
                put("productoEmoji", "🥑")
                put("productoCategoria", "Frutas")
                put("consumidorId", "cliente_123")
                put("productorId", "productor_123")
                put("cantidad", 4)
                put("total", 6.00)
                put("estado", "entregado")
                put("timestamp", 1776254400000L) // 15 Abril 2026
            }
            db.insert("pedidos", SQLiteDatabase.CONFLICT_REPLACE, order1)

            val order2 = ContentValues().apply {
                put("productoId", "2")
                put("productoNombre", "Fresas de Ambato")
                put("productoEmoji", "🍓")
                put("productoCategoria", "Frutas")
                put("consumidorId", "cliente_123")
                put("productorId", "productor_123")
                put("cantidad", 5)
                put("total", 10.00)
                put("estado", "aceptado")
                put("timestamp", 1778414400000L) // 10 Mayo 2026
            }
            db.insert("pedidos", SQLiteDatabase.CONFLICT_REPLACE, order2)

            val order3 = ContentValues().apply {
                put("productoId", "4")
                put("productoNombre", "Queso serrano")
                put("productoEmoji", "🧀")
                put("productoCategoria", "Lácteos")
                put("consumidorId", "cliente_123")
                put("productorId", "productor_456") // Lácteos San Antonio
                put("cantidad", 2)
                put("total", 7.00)
                put("estado", "pendiente")
                put("timestamp", 1780315200000L) // 1 Junio 2026
            }
            db.insert("pedidos", SQLiteDatabase.CONFLICT_REPLACE, order3)
        }
    }
}
