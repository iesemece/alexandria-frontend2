package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.response.LibroResponse;
import com.example.alexandriafrontend.session.SesionUsuario;
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
    private ListView<String> listaRecomendaciones;  // De momento, String (título), luego puedes poner LibroResponse

    @FXML
    private ListView<LibroResponse> listViewLibrosCompartidos;

    @FXML
    private AnchorPane contenido;

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
                        listaRecomendaciones.getItems().clear();
                        for (LibroResponse libro : response.body()) {
                            listaRecomendaciones.getItems().add(libro.getTitulo() + " — " + libro.getAutor());
                        }
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
