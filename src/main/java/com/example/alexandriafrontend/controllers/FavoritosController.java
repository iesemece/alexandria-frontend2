package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.ItemLibro;
import com.example.alexandriafrontend.utils.LectorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class FavoritosController {

    @FXML
    private FlowPane listaFavoritos;

    @FXML
    private AnchorPane contenido;

    private final ApiService apiService = ApiClient.getApiService();
    private SesionUsuario sesionUsuario;

    @FXML
    private void initialize() {
        sesionUsuario = SesionUsuario.getInstancia();
        if (sesionUsuario.getToken() != null) {
            cargarLibrosFavoritos();
        }
    }

    private void cargarLibrosFavoritos() {
        Call<List<Libro>> call = apiService.buscarLibrosFavoritos("Bearer " + sesionUsuario.getToken());
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        listaFavoritos.getChildren().clear();

                        for (Libro libro : response.body()) {
                            ItemLibro item = new ItemLibro(libro);

                            item.setOnMouseClicked(event -> {
                                // Si quieres que se elimine con UN solo clic izquierdo:
                                if (event.isPrimaryButtonDown()) {
                                    eliminarLibro(libro);
                                }
                            });

                            // Menú contextual para eliminar
                            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                            javafx.scene.control.MenuItem eliminarItem = new javafx.scene.control.MenuItem("Eliminar de favoritos");

                            eliminarItem.setOnAction(e -> eliminarLibro(libro));
                            contextMenu.getItems().add(eliminarItem);

                            item.setOnContextMenuRequested(e -> contextMenu.show(item, e.getScreenX(), e.getScreenY()));

                            item.setOnMouseClicked(event -> {
                                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY && event.getClickCount() == 2) {
                                    LectorHelper.pedirUrlYMostrarLibro(libro, contenido);
                                }
                            });

                            listaFavoritos.getChildren().add(item);
                        }
                    });
                } else {
                    System.out.println("Error al obtener favoritos.");
                }
            }

            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {
                System.out.println("Error de conexión con el servidor");
                t.printStackTrace();
            }
        });
    }

    private void eliminarLibro(Libro libro) {
        String token = SesionUsuario.getInstancia().getToken();
        if (token == null || libro == null) return;

        apiService.eliminarFavoritos("Bearer " + token, libro.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        listaFavoritos.getChildren().removeIf(node -> {
                            if (node instanceof ItemLibro) {
                                return ((ItemLibro) node).getLibro().getId() == libro.getId();
                            }
                            return false;
                        });
                    });
                } else {
                    System.err.println("No se pudo eliminar: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
