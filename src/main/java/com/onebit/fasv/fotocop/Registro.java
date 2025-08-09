package com.onebit.fasv.fotocop;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Registro {
    private final IntegerProperty id;
    private final StringProperty fecha;
    private final StringProperty detalle;
    private final DoubleProperty total;

    public Registro(int id, String fecha, String detalle, double total) {
        this.id = new SimpleIntegerProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.detalle = new SimpleStringProperty(detalle);
        this.total = new SimpleDoubleProperty(total);
    }

    // Getters para las propiedades (necesarios para la TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty fechaProperty() { return fecha; }
    public StringProperty detalleProperty() { return detalle; }
    public DoubleProperty totalProperty() { return total; }
}