package com.onebit.fasv.fotocop;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.DatabaseMetaData;

public class FotocopiadoraApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FotocopiadoraApp.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Registro de Fotocopiadora");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        DatabaseManager.conectar();
        launch(args);
    }
}
