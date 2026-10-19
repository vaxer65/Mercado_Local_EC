package com.example.holamundo.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.holamundo.model.Pedido
import com.example.holamundo.model.Producto
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 width
    private const val PAGE_HEIGHT = 842 // A4 height
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    // Helper class to manage PDF pages dynamically
    private class PdfBuilder(val context: Context, val pdfDocument: PdfDocument) {
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var currentY = MARGIN + 20f

        val paintText = Paint().apply {
            color = 0xFF1F1F2E.toInt() // Dark Slate
            textSize = 10f
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = 0xFF1F1F2E.toInt()
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        fun checkPageOverflow(neededHeight: Float) {
            if (currentY + neededHeight > PAGE_HEIGHT - MARGIN) {
                pdfDocument.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = MARGIN + 20f
                drawHeaderBackground()
            }
        }

        fun drawHeaderBackground() {
            val paintLine = Paint().apply {
                color = 0xFFE0E0E0.toInt()
                strokeWidth = 1f
            }
            canvas.drawLine(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN, paintLine)
            
            // Draw page number in footer
            val paintFooter = Paint().apply {
                color = 0xFF88889D.toInt()
                textSize = 8f
                isAntiAlias = true
            }
            canvas.drawText("Pág. $pageNum", PAGE_WIDTH - MARGIN - 30f, PAGE_HEIGHT - 20f, paintFooter)
        }

        fun finishDocument() {
            pdfDocument.finishPage(page)
        }
    }

    // Format helpers
    private val sdfDay = SimpleDateFormat("dd/MM/yyyy", Locale("es", "EC"))
    private val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale("es", "EC"))

    fun generarReporteProductor(
        context: Context,
        productorNombre: String,
        productorEmail: String,
        productos: List<Producto>,
        pedidos: List<Pedido>
    ) {
        val pdfDocument = PdfDocument()
        val builder = PdfBuilder(context, pdfDocument)
        
        // --- 1. HEADER ---
        drawMainHeader(builder, "REPORTE GENERAL DE VENTAS", "Finca/Productor: $productorNombre ($productorEmail)")

        // --- 2. RESUMEN DE VENTAS GENERALES ---
        val pedidosValidos = pedidos.filter { it.estado == "entregado" || it.estado == "aceptado" }
        val totalVendidos = pedidosValidos.sumOf { it.cantidad }
        val gananciasTotales = pedidosValidos.sumOf { it.total }

        builder.checkPageOverflow(80f)
        drawSummaryCard(
            builder,
            listOf(
                "Ganancias Totales" to "$${String.format(Locale.US, "%.2f", gananciasTotales)}",
                "Productos Vendidos" to "$totalVendidos unidades",
                "Pedidos Procesados" to "${pedidosValidos.size} pedidos"
            )
        )
        builder.currentY += 20f

        // --- 3. VENTAS AGRUPADAS POR MES ---
        builder.checkPageOverflow(40f)
        drawSectionTitle(builder, "Resumen de Ventas por Mes")
        
        val ventasPorMes = pedidosValidos.groupBy { 
            sdfMonth.format(Date(it.timestamp)).replaceFirstChar { char -> char.uppercase() }
        }

        if (ventasPorMes.isEmpty()) {
            builder.checkPageOverflow(20f)
            builder.canvas.drawText("No se registran ventas facturadas hasta el momento.", MARGIN, builder.currentY, builder.paintText)
            builder.currentY += 20f
        } else {
            ventasPorMes.forEach { (mes, listaPedidos) ->
                val cantMes = listaPedidos.sumOf { it.cantidad }
                val totalMes = listaPedidos.sumOf { it.total }
                
                builder.checkPageOverflow(25f)
                
                // Draw Mes info
                builder.canvas.drawText("• $mes:", MARGIN + 10f, builder.currentY, builder.paintBold)
                val mesDetalle = "$cantMes unidades vendidas  |  Total: $${String.format(Locale.US, "%.2f", totalMes)}"
                builder.canvas.drawText(mesDetalle, MARGIN + 140f, builder.currentY, builder.paintText)
                builder.currentY += 20f
            }
        }
        builder.currentY += 10f

        // --- 4. PRODUCTOS REGISTRADOS ---
        builder.checkPageOverflow(40f)
        drawSectionTitle(builder, "Catálogo de Productos Publicados")

        if (productos.isEmpty()) {
            builder.checkPageOverflow(20f)
            builder.canvas.drawText("No hay productos registrados en el catálogo.", MARGIN, builder.currentY, builder.paintText)
            builder.currentY += 20f
        } else {
            // Table Header
            builder.checkPageOverflow(25f)
            drawTableHeader(builder, listOf("Emoji", "Producto", "Categoría", "Precio", "Stock"))
            
            productos.forEach { prod ->
                builder.checkPageOverflow(20f)
                drawTableRow(
                    builder,
                    listOf(
                        prod.emoji,
                        prod.nombre,
                        prod.categoria,
                        "$${String.format(Locale.US, "%.2f", prod.precio)}",
                        "${prod.stock} ${prod.unidad}"
                    )
                )
            }
        }
        builder.currentY += 20f

        // --- 5. DETALLE DE TRANSACCIONES ---
        builder.checkPageOverflow(40f)
        drawSectionTitle(builder, "Historial de Pedidos Recibidos")

        if (pedidos.isEmpty()) {
            builder.checkPageOverflow(20f)
            builder.canvas.drawText("No se registran transacciones de pedidos.", MARGIN, builder.currentY, builder.paintText)
            builder.currentY += 20f
        } else {
            builder.checkPageOverflow(25f)
            drawTableHeader(builder, listOf("Fecha", "Producto", "Cant.", "Total", "Comprador", "Estado"))

            pedidos.forEach { ped ->
                builder.checkPageOverflow(20f)
                val fechaStr = sdfDay.format(Date(ped.timestamp))
                val compradorRef = "Id: ${ped.consumidorId.takeLast(5)}"
                val estadoVisual = ped.estado.replaceFirstChar { it.uppercase() }
                
                drawTableRow(
                    builder,
                    listOf(
                        fechaStr,
                        ped.productoNombre,
                        "${ped.cantidad}",
                        "$${String.format(Locale.US, "%.2f", ped.total)}",
                        compradorRef,
                        estadoVisual
                    )
                )
            }
        }

        // Close and save
        builder.finishDocument()
        saveAndOpenPdf(context, pdfDocument, "reporte_productor_$productorNombre.pdf")
    }

    fun generarReporteConsumidor(
        context: Context,
        consumidorNombre: String,
        consumidorEmail: String,
        pedidos: List<Pedido>
    ) {
        val pdfDocument = PdfDocument()
        val builder = PdfBuilder(context, pdfDocument)

        // --- 1. HEADER ---
        drawMainHeader(builder, "REPORTE DE COMPRAS", "Usuario: $consumidorNombre ($consumidorEmail)")

        // --- 2. RESUMEN GENERAL ---
        val pedidosCompletados = pedidos.filter { it.estado == "entregado" || it.estado == "aceptado" }
        val totalComprado = pedidosCompletados.sumOf { it.cantidad }
        val totalGastado = pedidosCompletados.sumOf { it.total }

        builder.checkPageOverflow(80f)
        drawSummaryCard(
            builder,
            listOf(
                "Total Gastado" to "$${String.format(Locale.US, "%.2f", totalGastado)}",
                "Productos Comprados" to "$totalComprado unidades",
                "Pedidos Realizados" to "${pedidos.size} pedidos"
            )
        )
        builder.currentY += 20f

        // --- 3. COMPRAS POR CATEGORÍA ---
        builder.checkPageOverflow(40f)
        drawSectionTitle(builder, "Resumen de Compras por Categoría")

        val comprasPorCategoria = pedidosCompletados.groupBy { 
            if (it.productoCategoria.isEmpty()) "General" else it.productoCategoria 
        }

        if (comprasPorCategoria.isEmpty()) {
            builder.checkPageOverflow(20f)
            builder.canvas.drawText("No se registran compras completadas en el historial.", MARGIN, builder.currentY, builder.paintText)
            builder.currentY += 20f
        } else {
            comprasPorCategoria.forEach { (categoria, lista) ->
                val cantCat = lista.sumOf { it.cantidad }
                val totalCat = lista.sumOf { it.total }

                builder.checkPageOverflow(25f)
                builder.canvas.drawText("• $categoria:", MARGIN + 10f, builder.currentY, builder.paintBold)
                val catDetalle = "$cantCat unidades compradas  |  Total Gastado: $${String.format(Locale.US, "%.2f", totalCat)}"
                builder.canvas.drawText(catDetalle, MARGIN + 140f, builder.currentY, builder.paintText)
                builder.currentY += 20f
            }
        }
        builder.currentY += 10f

        // --- 4. HISTORIAL DE COMPRAS ---
        builder.checkPageOverflow(40f)
        drawSectionTitle(builder, "Historial de Pedidos Realizados")

        if (pedidos.isEmpty()) {
            builder.checkPageOverflow(20f)
            builder.canvas.drawText("No has realizado compras aún.", MARGIN, builder.currentY, builder.paintText)
            builder.currentY += 20f
        } else {
            builder.checkPageOverflow(25f)
            drawTableHeader(builder, listOf("Fecha", "Producto", "Categoría", "Cant.", "Total", "Estado"))

            pedidos.forEach { ped ->
                builder.checkPageOverflow(20f)
                val fechaStr = sdfDay.format(Date(ped.timestamp))
                val cat = if (ped.productoCategoria.isEmpty()) "General" else ped.productoCategoria
                val estadoVisual = ped.estado.replaceFirstChar { it.uppercase() }

                drawTableRow(
                    builder,
                    listOf(
                        fechaStr,
                        ped.productoNombre,
                        cat,
                        "${ped.cantidad}",
                        "$${String.format(Locale.US, "%.2f", ped.total)}",
                        estadoVisual
                    )
                )
            }
        }

        // Close and save
        builder.finishDocument()
        saveAndOpenPdf(context, pdfDocument, "reporte_consumidor_$consumidorNombre.pdf")
    }

    // --- DRAW UTILITIES ---

    private fun drawMainHeader(builder: PdfBuilder, reportTitle: String, subtitle: String) {
        val paintHeaderBg = Paint().apply {
            color = 0xFF1E5E4A.toInt() // VerdePrincipal
        }
        // Top banner
        builder.canvas.drawRect(MARGIN, builder.currentY - 10f, PAGE_WIDTH - MARGIN, builder.currentY + 65f, paintHeaderBg)

        val paintTitle = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSub = Paint().apply {
            color = 0xFF9FE1CB.toInt() // Light mint green
            textSize = 10f
            isAntiAlias = true
        }

        val paintApp = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        builder.canvas.drawText("MERCADO LOCAL EC", MARGIN + 15f, builder.currentY + 15f, paintTitle)
        builder.canvas.drawText(reportTitle, MARGIN + 15f, builder.currentY + 35f, paintSub)
        builder.canvas.drawText(subtitle, MARGIN + 15f, builder.currentY + 52f, paintSub)
        
        val dateStr = "Fecha: " + SimpleDateFormat("dd 'de' MMMM 'yyyy, HH:mm'", Locale("es", "EC")).format(Date())
        builder.canvas.drawText(dateStr, PAGE_WIDTH - MARGIN - 180f, builder.currentY + 52f, paintSub)

        builder.currentY += 85f
    }

    private fun drawSectionTitle(builder: PdfBuilder, title: String) {
        val paintTitle = Paint().apply {
            color = 0xFF1E5E4A.toInt() // VerdePrincipal
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintLine = Paint().apply {
            color = 0xFF1E5E4A.toInt()
            strokeWidth = 1.5f
        }

        builder.canvas.drawText(title.uppercase(), MARGIN, builder.currentY, paintTitle)
        builder.canvas.drawLine(MARGIN, builder.currentY + 5f, PAGE_WIDTH - MARGIN, builder.currentY + 5f, paintLine)
        builder.currentY += 20f
    }

    private fun drawSummaryCard(builder: PdfBuilder, items: List<Pair<String, String>>) {
        val paintBg = Paint().apply {
            color = 0xFFE2EFE9.toInt() // Subtle green tint
        }
        val rectHeight = 50f
        builder.canvas.drawRect(MARGIN, builder.currentY, PAGE_WIDTH - MARGIN, builder.currentY + rectHeight, paintBg)

        val colWidth = CONTENT_WIDTH / items.size
        val paintTitle = Paint().apply {
            color = 0xFF575775.toInt() // Slate grey
            textSize = 8f
            isAntiAlias = true
        }
        val paintValue = Paint().apply {
            color = 0xFF1E5E4A.toInt() // Verde
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        items.forEachIndexed { index, (title, value) ->
            val startX = MARGIN + index * colWidth
            builder.canvas.drawText(title.uppercase(), startX + 15f, builder.currentY + 18f, paintTitle)
            builder.canvas.drawText(value, startX + 15f, builder.currentY + 36f, paintValue)
        }
        builder.currentY += rectHeight
    }

    private fun drawTableHeader(builder: PdfBuilder, columns: List<String>) {
        val paintBg = Paint().apply {
            color = 0xFFF0F0F3.toInt()
        }
        builder.canvas.drawRect(MARGIN, builder.currentY, PAGE_WIDTH - MARGIN, builder.currentY + 18f, paintBg)

        val colWidth = CONTENT_WIDTH / columns.size
        val paintCol = Paint().apply {
            color = 0xFF1F1F2E.toInt()
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        columns.forEachIndexed { index, colName ->
            builder.canvas.drawText(colName, MARGIN + index * colWidth + 5f, builder.currentY + 12f, paintCol)
        }
        builder.currentY += 18f
    }

    private fun drawTableRow(builder: PdfBuilder, cells: List<String>) {
        val colWidth = CONTENT_WIDTH / cells.size
        
        // Draw bottom divider line
        val paintLine = Paint().apply {
            color = 0xFFECECEF.toInt()
            strokeWidth = 0.8f
        }
        builder.canvas.drawLine(MARGIN, builder.currentY + 14f, PAGE_WIDTH - MARGIN, builder.currentY + 14f, paintLine)

        cells.forEachIndexed { index, cellText ->
            // Truncate text if it's too long
            var text = cellText
            val maxChars = when (cells.size) {
                4 -> 20
                5 -> 15
                6 -> 11
                else -> 25
            }
            if (text.length > maxChars) {
                text = text.take(maxChars - 2) + ".."
            }
            builder.canvas.drawText(text, MARGIN + index * colWidth + 5f, builder.currentY + 10f, builder.paintText)
        }
        builder.currentY += 15f
    }

    private fun saveAndOpenPdf(context: Context, pdfDocument: PdfDocument, filename: String) {
        try {
            val file = File(context.cacheDir, filename)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            // Open PDF with intent
            val uri = FileProvider.getUriForFile(
                context,
                "com.example.holamundo.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Ver Reporte PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar el PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
