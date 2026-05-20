package com.example.holamundo.model

object DatosPrueba {

    val productos = listOf(
        Producto(
            id = "1",
            nombre = "Aguacate orgánico",
            productor = "Finca El Paraíso",
            precio = 1.50,
            unidad = "unidad",
            descripcion = "Aguacates Hass cultivados sin pesticidas, cosechados en su punto óptimo de madurez. Ideales para guacamole, ensaladas o tostadas.",
            categoria = "Frutas",
            emoji = "🥑",
            stock = 48,
            provincia = "Loja"
        ),
        Producto(
            id = "2",
            nombre = "Fresas de Ambato",
            productor = "Huerto Don Luis",
            precio = 2.00,
            unidad = "libra",
            descripcion = "Fresas frescas cultivadas en las laderas del Tungurahua. Dulces, aromáticas y perfectas para postres o mermeladas.",
            categoria = "Frutas",
            emoji = "🍓",
            stock = 30,
            provincia = "Tungurahua"
        ),
        Producto(
            id = "3",
            nombre = "Zanahoria fresca",
            productor = "Don Rodrigo",
            precio = 0.80,
            unidad = "libra",
            descripcion = "Zanahorias cultivadas en suelos volcánicos de la sierra ecuatoriana. Crujientes y llenas de vitaminas.",
            categoria = "Verduras",
            emoji = "🥕",
            stock = 120,
            provincia = "Bolívar"
        ),
        Producto(
            id = "4",
            nombre = "Queso serrano",
            productor = "La Quesera",
            precio = 3.50,
            unidad = "unidad",
            descripcion = "Queso fresco artesanal elaborado con leche de vaca de la sierra. Perfecto para sopas, fritada o solo.",
            categoria = "Lácteos",
            emoji = "🧀",
            stock = 25,
            provincia = "Chimborazo"
        ),
        Producto(
            id = "5",
            nombre = "Limón sutil",
            productor = "Finca El Sol",
            precio = 1.00,
            unidad = "docena",
            descripcion = "Limones sutiles recién cortados, jugosos y aromáticos. Perfectos para ceviches, limonadas y aderezos.",
            categoria = "Frutas",
            emoji = "🍋",
            stock = 200,
            provincia = "Manabí"
        ),
        Producto(
            id = "6",
            nombre = "Cebolla paiteña",
            productor = "Agro San Pablo",
            precio = 0.60,
            unidad = "unidad",
            descripcion = "Cebolla paiteña de primer calidad, ideal para ensaladas, salsas y encurtidos típicos ecuatorianos.",
            categoria = "Verduras",
            emoji = "🧅",
            stock = 150,
            provincia = "Imbabura"
        ),
        Producto(
            id = "7",
            nombre = "Tejido otavaleño",
            productor = "Taller Mamá Rosa",
            precio = 18.00,
            unidad = "unidad",
            descripcion = "Tejido artesanal elaborado a mano con lana de oveja teñida con tintes naturales. Diseños tradicionales otavaleños.",
            categoria = "Artesanías",
            emoji = "🧶",
            stock = 12,
            provincia = "Imbabura"
        ),
        Producto(
            id = "8",
            nombre = "Vasija de barro",
            productor = "Taller Otavalo",
            precio = 12.00,
            unidad = "unidad",
            descripcion = "Vasija de barro cocido elaborada a mano. Perfecta para decoración o uso en cocina tradicional ecuatoriana.",
            categoria = "Artesanías",
            emoji = "🏺",
            stock = 8,
            provincia = "Imbabura"
        ),
        Producto(
            id = "9",
            nombre = "Miel de abeja pura",
            productor = "Apiario Los Andes",
            precio = 5.00,
            unidad = "frasco 250g",
            descripcion = "Miel de abeja 100% pura extraída de colmenas en los Andes ecuatorianos. Sin conservantes ni aditivos.",
            categoria = "Otros",
            emoji = "🍯",
            stock = 40,
            provincia = "Pichincha"
        ),
        Producto(
            id = "10",
            nombre = "Papa chola",
            productor = "Familia Chuqui",
            precio = 1.20,
            unidad = "libra",
            descripcion = "Papa chola cultivada en tierras altas de la sierra. La mejor para locro, tortillas y papas con cuero.",
            categoria = "Verduras",
            emoji = "🥔",
            stock = 300,
            provincia = "Carchi"
        ),
    )

    val categorias = listOf(
        "Todos", "Frutas", "Verduras", "Lácteos", "Artesanías", "Otros"
    )

    val provincias = listOf(
        "Todas", "Bolívar", "Chimborazo", "Cotopaxi", "Imbabura",
        "Loja", "Manabí", "Pichincha", "Tungurahua", "Carchi"
    )
}