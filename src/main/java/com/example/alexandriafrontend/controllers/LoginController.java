package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.response.LoginResponse;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class LoginController {

	@FXML
	private TextField tfEmail;

	@FXML
	private TextField tfContrasena;

	@FXML
	private Button btnIniciarSesion;

	@FXML
	private Button btnVolver;

	ApiService apiService = ApiClient.getApiService();

	@FXML
	private void iniciarSesion() {

		String email = tfEmail.getText();
		String contrasena = tfContrasena.getText();

		if (email.isEmpty() || contrasena.isEmpty()) {
			mostrarAlerta("Por favor, rellena todos los campos.");
			return;
		}

		Call<LoginResponse> call = apiService.login(email, contrasena);

		call.enqueue(new Callback<LoginResponse>() {
			@Override
			public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
				if (response.isSuccessful() && response.body() != null) {
					String token = response.body().getToken();

					SesionUsuario.getInstancia().iniciarSesionConToken(token);

					System.out.println("Login correcto. Bienvenido " + response.body().getPrimerNombre());
					Platform.runLater(() -> {
						Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
						Utils.cambiarPantalla(
								stage,
								"/com/example/alexandriafrontend/Menu.fxml",
								"/styles/menu.css", // Pon aquí la ruta a tu CSS del menú
								(MenuController m) -> {
									m.mostrarOpcionesPrivadas();
									m.cargarInicioConUsuario(SesionUsuario.getInstancia().getUsuarioActual());
								}
						);
					});

				} else {
					System.out.println("Credenciales inválidas. Inténtalo de nuevo.");
				}
			}

			@Override
			public void onFailure(Call<LoginResponse> call, Throwable t) {
				System.out.println("Error de conexión con el servidor");
				t.printStackTrace();
			}
		});
	}


	private void mostrarAlerta(String mensaje) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Login");
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
				"/styles/menu.css", // Ruta al CSS del menú
				c -> {}
		);
		System.out.println("Cargando Inicio.fxml");
	}
}
