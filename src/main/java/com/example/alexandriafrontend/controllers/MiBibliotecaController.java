package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.LectorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class MiBibliotecaController {
    @FXML
    private ListView<Libro> listaBiblioteca;

    @FXML
    private AnchorPane contenido;

    private ApiService apiService = ApiClient.getApiService();

    private SesionUsuario sesionUsuario;

    @FXML
    public void initialize() {
        sesionUsuario = SesionUsuario.getInstancia();
        if (sesionUsuario.getToken() != null){
            cargarLibrosLecturas();
        }
        listaBiblioteca.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Libro libroSeleccionado = listaBiblioteca.getSelectionModel().getSelectedItem();
                if (libroSeleccionado != null) {
                    LectorHelper.pedirUrlYMostrarLibro(libroSeleccionado, contenido);
                }
            }
        });

        listaBiblioteca.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText(libro.getTitulo());

                    javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
                    javafx.scene.control.MenuItem eliminarItem = new javafx.scene.control.MenuItem("Eliminar de favoritos");

                    eliminarItem.setOnAction(e -> eliminarLibro(libro));
                    contextMenu.getItems().add(eliminarItem);

                    setContextMenu(contextMenu);
                }
            }
        });
    }

    private void cargarLibrosLecturas() {
        Call<List<Libro>> call = apiService.buscarLibrosLecturas("Bearer " + sesionUsuario.getToken());
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Libro libro : response.body()) {
                        Libro nuevoLibro = new Libro(libro.getId(), libro.getTitulo(), libro.getAutor());
                        javafx.application.Platform.runLater(() -> listaBiblioteca.getItems().add(nuevoLibro));
                    }
                } else {
                    System.out.println("Credenciales inválidas. Inténtalo de nuevo.");
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

        apiService.eliminarLectura("Bearer " + token, libro.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> listaBiblioteca.getItems().remove(libro));
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
