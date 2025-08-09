package com.onebit.fasv.fotocop;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DetalleVenta {
    private final StringProperty descripcion;
    private final IntegerProperty cantidad;
    private final DoubleProperty precioUnitario;
    private final DoubleProperty subtotal;

    public DetalleVenta(String descripcion, int cantidad, double precioUnitario) {
        this.descripcion = new SimpleStringProperty(descripcion);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.precioUnitario = new SimpleDoubleProperty(precioUnitario);
        this.subtotal = new SimpleDoubleProperty(cantidad * precioUnitario);
    }

    // Getters y Properties (necesarios para la TableView)
    public String getDescripcion() { return descripcion.get(); }
    public StringProperty descripcionProperty() { return descripcion; }
    public int getCantidad() { return cantidad.get(); }
    public IntegerProperty cantidadProperty() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario.get(); }
    public DoubleProperty precioUnitarioProperty() { return precioUnitario; }
    public double getSubtotal() { return subtotal.get(); }
    public DoubleProperty subtotalProperty() { return subtotal; }
}