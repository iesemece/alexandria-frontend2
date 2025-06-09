package com.example.alexandriafrontend.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class Utils {

    public static <T> void cambiarPantalla(Stage stage, String rutaFXML, String rutaCSS, Consumer<T> controladorAccion) {
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(rutaFXML));
            Parent root = loader.load();
            T controller = loader.getController();
            controladorAccion.accept(controller);
            Scene scene = new Scene(root);

            // Añade el CSS si se ha pasado
            if (rutaCSS != null && !rutaCSS.isEmpty()) {
                scene.getStylesheets().add(Utils.class.getResource(rutaCSS).toExternalForm());
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void cargarPantalla(AnchorPane contenedor, String rutaFXML, String rutaCSS) {
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(rutaFXML));
            AnchorPane nuevoContenido = loader.load();

            contenedor.getChildren().clear();
            contenedor.getChildren().add(nuevoContenido);

            // Ajustar anclajes
            AnchorPane.setTopAnchor(nuevoContenido, 0.0);
            AnchorPane.setBottomAnchor(nuevoContenido, 0.0);
            AnchorPane.setLeftAnchor(nuevoContenido, 0.0);
            AnchorPane.setRightAnchor(nuevoContenido, 0.0);

            // Limpiar y aplicar CSS solo para el contenido
            contenedor.getStylesheets().clear();
            if (rutaCSS != null && !rutaCSS.isEmpty()) {
                contenedor.getStylesheets().add(Utils.class.getResource(rutaCSS).toExternalForm());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
