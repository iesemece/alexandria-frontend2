package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.model.Usuario;
import com.example.alexandriafrontend.utils.ItemLibro;
import com.example.alexandriafrontend.utils.LectorHelper;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.awt.event.ActionEvent;
import java.text.Normalizer;
import java.util.List;

public class InicioController {


    @FXML
    private FlowPane contenedorLibros;


    @FXML
    private Button btnIniciarSesion;

    @FXML
    private Button btnCrearCuenta;

    @FXML
    private Label lblNombreUsuario;

    @FXML
    private AnchorPane contenido;

    @FXML
    private HBox barraCategorias;

    private Button botonSeleccionado;

    private final ApiService apiService = ApiClient.getApiService();

    @FXML
    private void initialize() {
        cargarLibros();
        obtenerCategorias();

        Usuario usuarioLogueado = com.example.alexandriafrontend.session.SesionUsuario.getInstancia().getUsuarioActual();
        if (usuarioLogueado != null) {
            mostrarUsuarioLogueado(usuarioLogueado);
        }

    }

    private void cargarLibros() {
        Call<List<Libro>> call = apiService.obtenerTodosLibros();
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body();
                    mostrarLibros(libros);
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

    private void filtraPorCategoria(String categoria) {
        Call<List<Libro>> call = apiService.obtenerLibrosPorCategoria(categoria);
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body();
                    mostrarLibros(libros);
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

    private void mostrarLibros(List<Libro> libros) {
        Platform.runLater(() -> {
            contenedorLibros.getChildren().clear();
            for (Libro libro : libros) {
                ItemLibro item = new ItemLibro(libro);
                item.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        LectorHelper.pedirUrlYMostrarLibro(libro, contenido);
                    }
                });
                contenedorLibros.getChildren().add(item);
            }
        });
    }

    private void obtenerCategorias() {
        Call<List<String>> call = apiService.obtenerCategorias();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> categorias = response.body();
                    Platform.runLater(() -> {
                        barraCategorias.getChildren().clear();

                        for (String c : categorias) {
                            final String categoria = c;

                            Button btn = new Button(categoria);
                            btn.getStyleClass().add("categoria-button");

                            btn.setOnAction(e -> {
                                if (botonSeleccionado == btn) {
                                    btn.getStyleClass().remove("selected");
                                    botonSeleccionado = null;
                                    cargarLibros();
                                } else {
                                    if (botonSeleccionado != null) {
                                        botonSeleccionado.getStyleClass().remove("selected");
                                    }
                                    btn.getStyleClass().add("selected");
                                    botonSeleccionado = btn;
                                    filtraPorCategoria(categoria);
                                }
                            });

                            barraCategorias.getChildren().add(btn);
                        }
                    });
                } else {
                    System.out.println("Error al obtener categorías.");
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                System.out.println("Error de conexión con el servidor");
                t.printStackTrace();
            }
        });
    }

    public void mostrarUsuarioLogueado(Usuario usuario) {
        lblNombreUsuario.setText(usuario.getNombre() + " " + usuario.getApellido());
        lblNombreUsuario.setVisible(true);
        btnIniciarSesion.setVisible(false);
        btnCrearCuenta.setVisible(false);
    }

    @FXML
    public void handleButtonAction(javafx.event.ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source == btnIniciarSesion) {
            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            Utils.cambiarPantalla(stage, "/com/example/alexandriafrontend/Login.fxml", "/styles/Login.css", controller -> {});
        } else if (source == btnCrearCuenta) {
            Stage stage = (Stage) btnCrearCuenta.getScene().getWindow();
            Utils.cambiarPantalla(stage, "/com/example/alexandriafrontend/Registro.fxml", "/styles/Registro.css", controller -> {});
        }
    }


    private String getImageFileName(String categoria) {
        if (categoria == null || categoria.isEmpty()) return "default";
        return Normalizer.normalize(categoria, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replace(" ", "_")
                .replace("ó", "o")
                .replace("í", "i")
                .replace("é", "e")
                .replace("á", "a")
                .replace("ú", "u")
                .replace("ñ", "n");
    }


}
