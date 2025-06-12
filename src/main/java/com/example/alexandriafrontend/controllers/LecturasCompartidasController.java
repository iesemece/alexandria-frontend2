package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.response.LibroResponse;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.LectorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import retrofit2.Call;
import retrofit2.Response;
import java.util.List;

public class LecturasCompartidasController {

    @FXML
    private ListView<UsuarioListado> listViewUsuarios;

    @FXML
    private ListView<LibroResponse> listViewLibrosCompartidos;

    @FXML
    private AnchorPane contenido;

    @FXML
    private void initialize() {
        cargarUsuariosDisponibles();

        // Mostrar nombre y apellido en la lista de usuarios
        listViewUsuarios.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(UsuarioListado usuario, boolean empty) {
                super.updateItem(usuario, empty);
                if (empty || usuario == null) {
                    setText(null);
                } else {
                    setText(usuario.getPrimerNombre() + " " + usuario.getSegundoNombre());
                }
            }
        });

        // Al pinchar en un usuario, mostrar sus libros compartidos
        listViewUsuarios.setOnMouseClicked(event -> {
            UsuarioListado seleccionado = listViewUsuarios.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                mostrarLibrosCompartidosCon(seleccionado);
            }
        });

        // Mostrar los libros compartidos (título — autor)
        listViewLibrosCompartidos.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(LibroResponse libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                } else {
                    setText(libro.getTitulo() + " — " + libro.getAutor());
                }
            }
        });

        listViewLibrosCompartidos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LibroResponse libroSeleccionado = listViewLibrosCompartidos.getSelectionModel().getSelectedItem();
                UsuarioListado usuarioDestino = listViewUsuarios.getSelectionModel().getSelectedItem();
                if (libroSeleccionado != null && usuarioDestino != null) {
                    Long usuarioActualId = SesionUsuario.getInstancia().getIdUsuario();
                    Long usuarioDestinoId = usuarioDestino.getId();
                    Long libroId = libroSeleccionado.getId();
                    String token = SesionUsuario.getInstancia().getToken();

                    ApiService apiService = ApiClient.getApiService();
                    Call<Long> call = apiService.obtenerLecturaCompartidaId(
                            usuarioActualId,
                            usuarioDestinoId,
                            libroId,
                            "Bearer " + token
                    );

                    call.enqueue(new retrofit2.Callback<>() {
                        @Override
                        public void onResponse(Call<Long> call, Response<Long> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Long lecturaCompartidaId = response.body();
                                Platform.runLater(() -> {
                                    // Ahora lanzas el lector en modo colaborativo
                                    Libro libro = new Libro(
                                            libroSeleccionado.getId(),
                                            libroSeleccionado.getTitulo(),
                                            libroSeleccionado.getAutor()
                                            // añade más campos si tu constructor lo necesita
                                    );
                                    LectorHelper.pedirUrlYMostrarLibroColaborativo(libro, lecturaCompartidaId, contenido);
                                });
                            } else {
                                // Fallback: abre el lector normal
                                Platform.runLater(() -> {
                                    Libro libro = new Libro(
                                            libroSeleccionado.getId(),
                                            libroSeleccionado.getTitulo(),
                                            libroSeleccionado.getAutor()
                                    );
                                    LectorHelper.pedirUrlYMostrarLibro(libro, contenido);
                                });
                            }
                        }
                        @Override
                        public void onFailure(Call<Long> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }
        });

    }


    public void cargarUsuariosDisponibles() {
        String token = SesionUsuario.getInstancia().getToken();
        ApiService apiService = ApiClient.getApiService();
        Call<List<UsuarioListado>> call = apiService.obtenerUsuarios("Bearer " + token);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<UsuarioListado>> call, Response<List<UsuarioListado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        listViewUsuarios.getItems().setAll(response.body());
                    });
                } else {
                    System.out.println("Error al cargar usuarios: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioListado>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void mostrarLibrosCompartidosCon(UsuarioListado usuario) {
        Long usuarioActualId = SesionUsuario.getInstancia().getIdUsuario();
        Long usuarioDestinoId = usuario.getId();
        String token = SesionUsuario.getInstancia().getToken();

        ApiService apiService = ApiClient.getApiService();
        Call<List<LibroResponse>> call = apiService.obtenerLibrosCompartidos(
                usuarioActualId,
                usuarioDestinoId,
                "Bearer " + token
        );

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<LibroResponse>> call, Response<List<LibroResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        listViewLibrosCompartidos.getItems().setAll(response.body());
                    });
                } else {
                    System.out.println("Error al cargar libros compartidos: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<List<LibroResponse>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
