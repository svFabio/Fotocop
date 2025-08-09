package com.onebit.fasv.fotocop;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:fotocopiadora.db";

    /**
     * Se conecta a la BD y se asegura de que las tablas 'ventas' y 'detalles_venta' existan.
     */
    public static void conectar() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {

            System.out.println("Conexion a SQLite establecida.");

            // Tabla principal para cada transacción
            String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas (" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  fecha TEXT NOT NULL," +
                    "  total REAL NOT NULL" +
                    ");";
            stmt.execute(sqlVentas);

            // Tabla para cada línea de producto dentro de una transacción
            String sqlDetalles = "CREATE TABLE IF NOT EXISTS detalles_venta (" +
                    "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  venta_id INTEGER NOT NULL," +
                    "  descripcion TEXT NOT NULL," +
                    "  cantidad INTEGER NOT NULL," +
                    "  precio_unitario REAL NOT NULL," +
                    "  subtotal REAL NOT NULL," +
                    "  FOREIGN KEY (venta_id) REFERENCES ventas(id)" +
                    ");";
            stmt.execute(sqlDetalles);

            System.out.println("Tablas 'ventas' y 'detalles_venta' listas.");

        } catch (SQLException e) {
            System.err.println("Error al conectar o crear las tablas: " + e.getMessage());
        }
    }

    /**
     * Guarda una venta completa (la venta principal y todos sus detalles) en una transacción.
     * Esto asegura que si algo falla, no se guarda nada a medias.
     */
    public static boolean guardarVentaCompleta(String fecha, double total, List<DetalleVenta> detalles) {
        String sqlVenta = "INSERT INTO ventas(fecha, total) VALUES(?,?)";
        String sqlDetalle = "INSERT INTO detalles_venta(venta_id, descripcion, cantidad, precio_unitario, subtotal) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            // Desactivamos el auto-commit para manejar la transacción manualmente
            conn.setAutoCommit(false);

            long ventaId = -1;

            // 1. Insertar la venta principal y obtener su ID generado
            try (PreparedStatement pstmtVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS)) {
                pstmtVenta.setString(1, fecha);
                pstmtVenta.setDouble(2, total);
                pstmtVenta.executeUpdate();

                ResultSet generatedKeys = pstmtVenta.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ventaId = generatedKeys.getLong(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID de la venta.");
                }
            }

            // 2. Insertar cada detalle asociándolo con el ID de la venta
            try (PreparedStatement pstmtDetalle = conn.prepareStatement(sqlDetalle)) {
                for (DetalleVenta detalle : detalles) {
                    pstmtDetalle.setLong(1, ventaId);
                    pstmtDetalle.setString(2, detalle.getDescripcion());
                    pstmtDetalle.setInt(3, detalle.getCantidad());
                    pstmtDetalle.setDouble(4, detalle.getPrecioUnitario());
                    pstmtDetalle.setDouble(5, detalle.getSubtotal());
                    pstmtDetalle.addBatch();
                }
                pstmtDetalle.executeBatch();
            }

            // Si todo salió bien, confirmamos la transacción
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al guardar la venta completa: " + e.getMessage());
            // Si algo falló, revertimos todo
            // (La conexión se cierra automáticamente y revierte los cambios no confirmados)
            return false;
        }
    }

    public static List<Venta> cargarVentasPorMes(int anio, int mes) {
        List<Venta> ventas = new ArrayList<>();
        String anioMes = String.format("%d-%02d", anio, mes);
        String sql = "SELECT id, fecha, total FROM ventas WHERE strftime('%Y-%m', fecha) = ? ORDER BY fecha ASC";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, anioMes);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ventas.add(new Venta(
                        rs.getInt("id"),
                        rs.getString("fecha"),
                        rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar ventas: " + e.getMessage());
        }
        return ventas;
    }

    /**
     * Carga los detalles de una venta específica a partir de su ID.
     */
    public static List<DetalleVenta> cargarDetallesPorVenta(int ventaId) {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT descripcion, cantidad, precio_unitario FROM detalles_venta WHERE venta_id = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ventaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detalles.add(new DetalleVenta(
                        rs.getString("descripcion"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar detalles de venta: " + e.getMessage());
        }
        return detalles;
    }

    // Aquí irán los métodos para cargar las ventas y detalles (los añadiremos después)
}