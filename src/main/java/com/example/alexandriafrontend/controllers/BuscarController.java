package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.response.LibroResponse;
import com.example.alexandriafrontend.utils.LectorHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class BuscarController {

    @FXML
    private ListView<Libro> listalibros;

    @FXML
    private TextField txtBuscar;

    @FXML
    private AnchorPane contenido;

    private ApiService apiService = ApiClient.getApiService();

    @FXML
    private void initialize() {

        cargarLibros();

        // Añadir listener para buscar en tiempo real
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrarLibros(newValue);
        });

        listalibros.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Libro libroSeleccionado = listalibros.getSelectionModel().getSelectedItem();
                if (libroSeleccionado != null) {
                    LectorHelper.pedirUrlYMostrarLibro(libroSeleccionado, contenido);
                }
            }
        });
    }

    private void cargarLibros() {
        Call<List<LibroResponse>> call = apiService.obtenerTodosLibros();
        call.enqueue(new Callback<List<LibroResponse>>() {
            @Override
            public void onResponse(Call<List<LibroResponse>> call, Response<List<LibroResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (LibroResponse libro : response.body()) {
                        Libro nuevoLibro = new Libro(libro.getId(), libro.getTitulo(), libro.getAutor());
                        javafx.application.Platform.runLater(() -> listalibros.getItems().add(nuevoLibro));
                    }
                } else {
                    System.out.println("Credenciales inválidas. Inténtalo de nuevo.");
                }
            }

            @Override
            public void onFailure(Call<List<LibroResponse>> call, Throwable t) {
                System.out.println("Error de conexión con el servidor");
                t.printStackTrace();
            }
        });
    }

    private void filtrarLibros(String filtro) {
        listalibros.getItems().clear();

        Call<List<LibroResponse>> call = apiService.buscarLibros(filtro);
        call.enqueue(new Callback<List<LibroResponse>>() {
            @Override
            public void onResponse(Call<List<LibroResponse>> call, Response<List<LibroResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (LibroResponse libro : response.body()) {
                        Libro nuevoLibro = new Libro(libro.getId(), libro.getTitulo(), libro.getAutor());
                        javafx.application.Platform.runLater(() -> listalibros.getItems().add(nuevoLibro));
                    }
                } else {
                    System.out.println("Credenciales inválidas. Inténtalo de nuevo.");
                }
            }
            @Override
            public void onFailure(Call<List<LibroResponse>> call, Throwable t) {
                System.out.println("Error de conexión con el servidor");
                t.printStackTrace();
            }
        });
    }
}
