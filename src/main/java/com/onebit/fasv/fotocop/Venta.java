package com.onebit.fasv.fotocop;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Venta {
    private final IntegerProperty id;
    private final StringProperty fecha;
    private final DoubleProperty total;

    public Venta(int id, String fecha, double total) {
        this.id = new SimpleIntegerProperty(id);
        this.fecha = new SimpleStringProperty(fecha);
        this.total = new SimpleDoubleProperty(total);
    }

    // Getters y Properties
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public StringProperty fechaProperty() { return fecha; }
    public DoubleProperty totalProperty() { return total; }
}