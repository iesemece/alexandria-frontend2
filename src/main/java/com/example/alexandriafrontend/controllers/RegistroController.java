package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.net.URL;

public class RegistroController {

    @FXML
    private TextField tfPrimer_Nombre;

    @FXML
    private TextField tfApellido;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfContraseña;

    @FXML
    private ComboBox<String> cbRol;

    @FXML
    private Button btnRegistrarse;

    @FXML
    private Button btnVolver;

    private final ApiService apiService = ApiClient.getApiService();

    @FXML
    private void initialize() {

        cbRol.getItems().addAll("Alumno", "Profesor", "Administrador");
    }

    @FXML
    private void handleRegistrarse() {

        String nombre = tfPrimer_Nombre.getText();
        String apellido = tfApellido.getText();
        String email = tfEmail.getText();
        String contrasena = tfContraseña.getText();
        String rol = cbRol.getValue() != null ? cbRol.getValue().toUpperCase() : "";

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || contrasena.isEmpty() || !esRolValido(rol)) {
            mostrarAlerta("Por favor, completa todos los campos.");
            return;
        }

         nombre = tfPrimer_Nombre.getText().substring(0, 1).toUpperCase() + tfPrimer_Nombre.getText().substring(1).toLowerCase();
         apellido = tfApellido.getText().substring(0, 1).toUpperCase() + tfApellido.getText().substring(1).toLowerCase();
         email = tfEmail.getText();
         contrasena = tfContraseña.getText();
         rol = cbRol.getValue().toUpperCase();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || contrasena.isEmpty() || !esRolValido(rol)) {
            mostrarAlerta("Por favor, completa todos los campos.");
            return;
        }

        Call<ResponseBody> call = apiService.registrar(
                nombre,
                apellido,
                email,
                contrasena,
                rol
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    System.out.println("Usuario registrado correctamente");
                    Platform.runLater(() -> {
                        Stage stage = (Stage) btnRegistrarse.getScene().getWindow();
                        Utils.cambiarPantalla(
                                stage,
                                "/com/example/alexandriafrontend/Login.fxml",
                                "/styles/Login.css", // Aquí tu CSS para Login
                                c -> {}
                        );
                        System.out.println("Cargando Login.fxml");
                    });
                } else {
                    System.out.println("Peticion fallida");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Error de conexión con el servidor");
                t.printStackTrace();
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Icono minimalista
        Text icon = new Text("i"); // Letra "i" estilizada
        icon.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-fill: white;");
        StackPane graphic = new StackPane(icon);
        graphic.setStyle("-fx-background-color: #3498DB; -fx-background-radius: 50%; -fx-min-width: 36px; -fx-min-height: 36px;");
        alert.setGraphic(graphic);

        // Carga CSS
        URL cssUrl = getClass().getResource("/styles/alertas.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }

        alert.showAndWait();
    }

    @FXML
    private void volverAlInicio() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        Utils.cambiarPantalla(
                stage,
                "/com/example/alexandriafrontend/Menu.fxml",
                "/styles/menu.css",
                c -> {}
        );
        System.out.println("Cargando Inicio.fxml");
    }

    @FXML
    private void irAlLogin() {
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        Utils.cambiarPantalla(
                stage,
                "/com/example/alexandriafrontend/Login.fxml",
                "/styles/Login.css",
                c -> {}
        );
        System.out.println("Cargando Login.fxml");
    }

    private boolean esRolValido(String role) {
        return role.equalsIgnoreCase("ALUMNO") || role.equalsIgnoreCase("PROFESOR");
    }
}
