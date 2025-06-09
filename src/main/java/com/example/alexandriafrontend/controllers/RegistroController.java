package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        // Añadimos roles al ComboBox
        cbRol.getItems().addAll("Alumno", "Profesor", "Administrador");
    }

    @FXML
    private void handleRegistrarse() {
        String nombre = tfPrimer_Nombre.getText().substring(0, 1).toUpperCase() + tfPrimer_Nombre.getText().substring(1).toLowerCase();
        String apellido = tfApellido.getText().substring(0, 1).toUpperCase() + tfApellido.getText().substring(1).toLowerCase();
        String email = tfEmail.getText();
        String contrasena = tfContraseña.getText();
        String rol = cbRol.getValue().toUpperCase();

        // Validación básica
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

    private boolean esRolValido(String role) {
        return role.equalsIgnoreCase("ALUMNO") || role.equalsIgnoreCase("PROFESOR");
    }
}
