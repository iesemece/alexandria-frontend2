package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.ItemLibro;
import com.example.alexandriafrontend.utils.LectorHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.ListView;
import retrofit2.Call;
import retrofit2.Response;
import java.util.List;

public class LecturasCompartidasController {

    @FXML
    private ListView<UsuarioListado> listViewUsuarios;

    @FXML
    private FlowPane listViewLibrosCompartidos;

    @FXML
    private AnchorPane contenido;

    private final ApiService apiService = ApiClient.getApiService();

    @FXML
    private void initialize() {
        cargarUsuariosDisponibles();

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

        listViewUsuarios.setOnMouseClicked(event -> {
            UsuarioListado seleccionado = listViewUsuarios.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                mostrarLibrosCompartidosCon(seleccionado);
            }
        });
    }

    public void cargarUsuariosDisponibles() {
        String token = SesionUsuario.getInstancia().getToken();
        Call<List<UsuarioListado>> call = apiService.obtenerUsuarios("Bearer " + token);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<UsuarioListado>> call, Response<List<UsuarioListado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> listViewUsuarios.getItems().setAll(response.body()));
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

        Call<List<Libro>> call = apiService.obtenerLibrosCompartidos(usuarioActualId, usuarioDestinoId, "Bearer " + token);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        listViewLibrosCompartidos.getChildren().clear();
                        for (Libro libro : response.body()) {
                            ItemLibro item = new ItemLibro(libro);

                            item.setOnMouseClicked(event -> {
                                if (event.getClickCount() == 2) {
                                    obtenerYMostrarLecturaCompartida(libro, usuario);
                                }
                            });

                            listViewLibrosCompartidos.getChildren().add(item);
                        }
                    });
                } else {
                    System.out.println("Error al cargar libros compartidos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void obtenerYMostrarLecturaCompartida(Libro libroSeleccionado, UsuarioListado usuarioDestino) {
        Long usuarioActualId = SesionUsuario.getInstancia().getIdUsuario();
        Long usuarioDestinoId = usuarioDestino.getId();
        Long libroId = libroSeleccionado.getId();
        String token = SesionUsuario.getInstancia().getToken();

        Call<Long> call = apiService.obtenerLecturaCompartidaId(usuarioActualId, usuarioDestinoId, libroId, "Bearer " + token);

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Long lecturaCompartidaId = response.body();
                        LectorHelper.pedirUrlYMostrarLibroColaborativo(libroSeleccionado, lecturaCompartidaId, contenido);
                    } else {
                        LectorHelper.pedirUrlYMostrarLibro(libroSeleccionado, contenido);
                    }
                });
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
