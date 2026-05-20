package com.example.holamundo.model.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.holamundo.model.DatosPrueba
import com.example.holamundo.model.Mensaje
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.Producto
import com.example.holamundo.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Usuario::class, Producto::class, Pedido::class, Mensaje::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun mensajeDao(): MensajeDao

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
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dbInstance = getDatabase(context)
                val usuarioDao = dbInstance.usuarioDao()
                val productoDao = dbInstance.productoDao()

                // Insert default users
                usuarioDao.registrar(
                    Usuario(
                        uid = "cliente_123",
                        nombre = "Cliente de Prueba",
                        email = "cliente@test.com",
                        password = "123456",
                        rol = "CONSUMIDOR"
                    )
                )
                usuarioDao.registrar(
                    Usuario(
                        uid = "productor_123",
                        nombre = "Productor de Prueba",
                        email = "productor@test.com",
                        password = "123456",
                        rol = "PRODUCTOR"
                    )
                )

                // Insert default products mapped to our default producer
                val productosMapeados = DatosPrueba.productos.map {
                    it.copy(productorId = "productor_123")
                }
                productoDao.insertar(*productosMapeados.toTypedArray())
            }
        }
    }
}
