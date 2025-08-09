package com.onebit.fasv.fotocop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FotocopiadoraController {

    //<editor-fold desc="Variables FXML">
    @FXML private Label mesLabel;
    @FXML private TableView<Venta> ventasTable;
    @FXML private TableColumn<Venta, Number> colVentaId;
    @FXML private TableColumn<Venta, String> colVentaFecha;
    @FXML private TableColumn<Venta, Number> colVentaTotal;
    @FXML private TableView<DetalleVenta> detallesTable;
    @FXML private TableColumn<DetalleVenta, String> colDetalleDesc;
    @FXML private TableColumn<DetalleVenta, Number> colDetalleCant;
    @FXML private TableColumn<DetalleVenta, Number> colDetalleSubtotal;
    @FXML private ComboBox<String> detalleCombo;
    @FXML private TextField cantidadField;
    @FXML private TextField otroField;
    @FXML private TextField otroPrecioField;
    @FXML private TableView<DetalleVenta> carritoTable;
    @FXML private TableColumn<DetalleVenta, String> colCarritoDesc;
    @FXML private TableColumn<DetalleVenta, Number> colCarritoCant;
    @FXML private TableColumn<DetalleVenta, Number> colCarritoSubtotal;
    @FXML private Label totalVentaLabel;
    @FXML private TableColumn<DetalleVenta, Void> colCarritoAccion;
    //</editor-fold>

    private LocalDate fechaActual;
    private final DateTimeFormatter formatterMes = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
    private final ObservableList<DetalleVenta> carritoItems = FXCollections.observableArrayList();
    private final Map<String, Double> precios = Map.of(
            "Fotocopia B/N", 0.30,
            "Fotocopia C.I.", 0.60,
            "Impresión B/N", 1.00,
            "Impresión Color", 2.00,
            "Impresión C.I Color", 2.00
    );

    @FXML
    public void initialize() {
        // --- Configurar Tablas ---
        configurarTablaVentas();
        configurarTablaDetalles();
        configurarTablaCarrito();

        // --- Llenar ComboBox ---
        detalleCombo.getItems().addAll(precios.keySet());

        // --- Cargar Vista Inicial ---
        fechaActual = LocalDate.now();
        cargarVentasDelMes();
    }

    //<editor-fold desc="Configuración de Tablas">
    private void configurarTablaVentas() {
        colVentaId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colVentaFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());
        colVentaTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty());

        // ---  FORMATEAR LA FECHA ---
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colVentaFecha.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    LocalDateTime date = LocalDateTime.parse(item, dbFormatter);
                    setText(date.format(displayFormatter));
                }
            }
        });

        // Listener para mostrar detalles cuando se selecciona una venta
        ventasTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                List<DetalleVenta> detalles = DatabaseManager.cargarDetallesPorVenta(newSelection.getId());
                detallesTable.setItems(FXCollections.observableArrayList(detalles));
            } else {
                detallesTable.getItems().clear();
            }
        });
    }

    private void configurarTablaDetalles() {
        colDetalleDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        colDetalleCant.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colDetalleSubtotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty());
    }

    private void configurarTablaCarrito() {
        colCarritoDesc.setCellValueFactory(cellData -> cellData.getValue().descripcionProperty());
        colCarritoCant.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colCarritoSubtotal.setCellValueFactory(cellData -> cellData.getValue().subtotalProperty());
        carritoTable.setItems(carritoItems);

        // --- CÓDIGO PARA CREAR LA COLUMNA DE BOTONES ---
        Callback<TableColumn<DetalleVenta, Void>, TableCell<DetalleVenta, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<DetalleVenta, Void> call(final TableColumn<DetalleVenta, Void> param) {
                final TableCell<DetalleVenta, Void> cell = new TableCell<>() {

                    private final Button btn = new Button("Eliminar");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            // Obtiene el item de la fila actual y lo elimina de la lista
                            DetalleVenta item = getTableView().getItems().get(getIndex());
                            carritoItems.remove(item);
                            actualizarTotalCarrito();
                        });
                        // Le asignamos una clase de estilo para que la controle el CSS
                        btn.getStyleClass().add("delete-button");
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };

        colCarritoAccion.setCellFactory(cellFactory);
        // --- FIN DEL CÓDIGO DE LA COLUMNA ---
    }
    //</editor-fold>

    //<editor-fold desc="Lógica del Carrito y Venta">
    @FXML
    private void onAddItem() {
        String detalle = detalleCombo.getValue();
        if (detalle == null || cantidadField.getText().isEmpty()) return;

        try {
            int cantidad = Integer.parseInt(cantidadField.getText());
            double precioUnitario = precios.get(detalle);
            carritoItems.add(new DetalleVenta(detalle, cantidad, precioUnitario));
            actualizarTotalCarrito();
            detalleCombo.getSelectionModel().clearSelection();
            cantidadField.clear();
        } catch (NumberFormatException e) {
            // Mostrar alerta de cantidad inválida
        }
    }

    @FXML
    private void onAddOtro() {
        String descripcion = otroField.getText();
        if (descripcion.isEmpty() || otroPrecioField.getText().isEmpty()) return;

        try {
            double precio = Double.parseDouble(otroPrecioField.getText().replace(",","."));
            // Para "otros", la cantidad es siempre 1
            carritoItems.add(new DetalleVenta(descripcion, 1, precio));
            actualizarTotalCarrito();
            otroField.clear();
            otroPrecioField.clear();
        } catch (NumberFormatException e) {
            // Mostrar alerta de precio inválido
        }
    }

    @FXML
    private void onFinalizarVenta() {
        if (carritoItems.isEmpty()) return;

        String fechaParaBD = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        double totalFinal = carritoItems.stream().mapToDouble(DetalleVenta::getSubtotal).sum();

        boolean exito = DatabaseManager.guardarVentaCompleta(fechaParaBD, totalFinal, new ArrayList<>(carritoItems));

        if (exito) {
            cargarVentasDelMes();
            carritoItems.clear();
            actualizarTotalCarrito();
        } else {
            // Mostrar alerta de error al guardar
        }
    }

    private void actualizarTotalCarrito() {
        double total = carritoItems.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        totalVentaLabel.setText(String.format("TOTAL: %.2f Bs.", total));
    }


    //<editor-fold desc="Navegación de Meses">
    @FXML
    private void onMesAnterior() {
        fechaActual = fechaActual.minusMonths(1);
        cargarVentasDelMes();
    }

    @FXML
    private void onMesSiguiente() {
        fechaActual = fechaActual.plusMonths(1);
        cargarVentasDelMes();
    }

    private void cargarVentasDelMes() {
        mesLabel.setText(fechaActual.format(formatterMes).toUpperCase());
        List<Venta> ventasDelMes = DatabaseManager.cargarVentasPorMes(fechaActual.getYear(), fechaActual.getMonthValue());
        ventasTable.setItems(FXCollections.observableArrayList(ventasDelMes));
        detallesTable.getItems().clear(); // Limpiar detalles al cambiar de mes
    }
    //</editor-fold>

    @FXML
    private void onExportarPDF() {
        try {
            // Obtenemos la lista de ventas que ya está cargada en la tabla
            List<Venta> ventasDelMes = ventasTable.getItems();
            if (ventasDelMes.isEmpty()) {
                // Opcional: Mostrar alerta de que no hay datos
                System.out.println("No hay datos para exportar.");
                return;
            }

            ReporteGenerator.generarPdfMensual(fechaActual, ventasDelMes);

            // Mostrar una alerta de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exportación Exitosa");
            alert.setHeaderText(null);
            alert.setContentText("El reporte PDF ha sido guardado en tu escritorio.");
            alert.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar alerta de error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Exportación");
            alert.setHeaderText("No se pudo generar el archivo PDF.");
            alert.setContentText("Ocurrió un error: " + e.getMessage());
            alert.showAndWait();
        }
    }

}