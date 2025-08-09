package com.onebit.fasv.fotocop;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReporteGenerator {

    public static void generarPdfMensual(LocalDate fecha, List<Venta> ventas) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // --- Configuración inicial ---
                float y = 750;
                final float margin = 50;
                final float rowHeight = 15;
                final float detailColumnWidth = 240;

                // --- Título ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(margin, y);
                String titulo = "Reporte Detallado de Ventas - " + fecha.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES")));
                contentStream.showText(titulo.toUpperCase());
                contentStream.endText();
                y -= 40;

                // --- Cabeceras de la Tabla ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Nº");
                contentStream.newLineAtOffset(50, 0);
                contentStream.showText("Fecha");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Detalle");
                contentStream.newLineAtOffset(250, 0);
                contentStream.showText("Total (Bs.)");
                contentStream.endText();
                y -= rowHeight;

                // --- Filas de Datos ---
                PDType1Font textFont = PDType1Font.HELVETICA;
                float textFontSize = 10;
                contentStream.setFont(textFont, textFontSize);
                double totalMes = 0;

                for (Venta venta : ventas) {
                    // Formatear detalles
                    List<DetalleVenta> detalles = DatabaseManager.cargarDetallesPorVenta(venta.getId());
                    List<String> detallesFormateados = new ArrayList<>();
                    List<String> serviciosEstandar = List.of("Fotocopia B/N", "Fotocopia Color", "Impresión B/N", "Impresión Color");
                    for (DetalleVenta detalle : detalles) {
                        if (serviciosEstandar.contains(detalle.getDescripcion())) {
                            detallesFormateados.add(String.format("%s x%d", detalle.getDescripcion(), detalle.getCantidad()));
                        } else {
                            detallesFormateados.add(detalle.getDescripcion());
                        }
                    }
                    String detallesConcatenados = String.join(", ", detallesFormateados);

                    // Dividir el texto de detalle en varias líneas si es necesario
                    List<String> lines = new ArrayList<>();
                    splitTextIntoLines(detallesConcatenados, textFont, textFontSize, detailColumnWidth, lines);

                    // Dibujar la fila, ajustando la altura dinámicamente
                    float currentY = y;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, currentY);
                    contentStream.showText(String.valueOf(venta.getId()));
                    contentStream.newLineAtOffset(50, 0);
                    contentStream.showText(formatDateForDisplay(venta.fechaProperty().get()));
                    contentStream.newLineAtOffset(400, 0);
                    contentStream.showText(String.format("%.2f", venta.totalProperty().get()));
                    contentStream.endText();

                    for (String line : lines) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + 200, currentY);
                        contentStream.showText(line);
                        contentStream.endText();
                        currentY -= rowHeight;
                    }

                    y = currentY - (rowHeight / 2);
                    totalMes += venta.totalProperty().get();
                }

                // --- Línea y Total Final ---
                y -= 20;
                contentStream.moveTo(margin, y);
                contentStream.lineTo(margin + 500, y);
                contentStream.stroke();
                y -= 20;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(margin + 280, y);
                contentStream.showText(String.format("TOTAL GENERAL DEL MES: %.2f Bs.", totalMes));
                contentStream.endText();
            }

            // Guardar el archivo en el escritorio del usuario
            String nombreArchivo = "Informe-" + fecha.format(DateTimeFormatter.ofPattern("yyyy-MM")) + ".pdf";
            String rutaEscritorio = System.getProperty("user.home") + "/Desktop/";
            document.save(rutaEscritorio + nombreArchivo);
        }
    }

    private static void splitTextIntoLines(String text, PDType1Font font, float fontSize, float maxWidth, List<String> lines) throws IOException {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            float width;
            if (line.length() > 0) {
                width = font.getStringWidth(line + " " + word) / 1000 * fontSize;
            } else {
                width = font.getStringWidth(word) / 1000 * fontSize;
            }

            if (width > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                }
                line.append(word);
            }
        }
        lines.add(line.toString());
    }

    private static String formatDateForDisplay(String dbDate) {
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime fecha = LocalDateTime.parse(dbDate, dbFormatter);
        return fecha.format(displayFormatter);
    }
}