package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.model.Usuario;
import com.example.alexandriafrontend.utils.LectorHelper;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

public class InicioController {

    @FXML
    private ListView<Libro> listalibros;

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


    private ApiService apiService = ApiClient.getApiService();


    @FXML
    private void initialize() {

        cargarLibros();
        obtenerCategorias();


        listalibros.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Libro libroSeleccionado = listalibros.getSelectionModel().getSelectedItem();
                if (libroSeleccionado != null) {
                    LectorHelper.pedirUrlYMostrarLibro(libroSeleccionado, contenido);
                }
            }
        });

        listalibros.setCellFactory(lv -> new javafx.scene.control.ListCell<Libro>() {
            @Override
            protected void updateItem(Libro libro, boolean empty) {
                super.updateItem(libro, empty);
                if (empty || libro == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/alexandriafrontend/LibroItem.fxml"));
                        AnchorPane pane = loader.load();

                        ImageView imgCategoria = (ImageView) pane.lookup("#imgCategoria");
                        Label lblTitulo = (Label) pane.lookup("#lblTitulo");
                        Label lblAutor = (Label) pane.lookup("#lblAutor");
                        Label lblCategoria = (Label) pane.lookup("#lblCategoria");

                        lblTitulo.setText(libro.getTitulo());
                        lblAutor.setText(libro.getAutor());
                        String categoria = libro.getCategoria();
                        lblCategoria.setText(categoria != null ? categoria : "");

                        String nombreBase = getImageFileName(categoria);
                        String urlImg = "/image/" + nombreBase + ".png";
                        InputStream is = getClass().getResourceAsStream(urlImg);
                        if (is == null) {
                            is = getClass().getResourceAsStream("/image/default.png");
                        }
                        imgCategoria.setImage(new javafx.scene.image.Image(is));

                        setGraphic(pane);
                    } catch (Exception e) {
                        e.printStackTrace();
                        setText(libro.toString());
                    }
                }
            }
        });

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
                    Platform.runLater(() -> {
                        listalibros.getItems().clear();
                        for (Libro libro : libros) {
                            listalibros.getItems().add(libro);
                        }
                    });
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
    private String getImageFileName(String categoria) {
        if (categoria == null || categoria.isEmpty()) return "default";
        // Quitar tildes y pasar a minúsculas
        String nombre = Normalizer.normalize(categoria, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replace(" ", "_")
                .replace("ó", "o")
                .replace("í", "i")
                .replace("é", "e")
                .replace("á", "a")
                .replace("ú", "u")
                .replace("ñ", "n");
        return nombre;
    }


    private void obtenerCategorias() {
        Call<List<String>> call = apiService.obtenerCategorias();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> categorias = response.body();

                    Platform.runLater(() -> {
                        barraCategorias.getChildren().clear(); // Limpiar botones anteriores

                        for (String c : categorias) {
                            final String categoria = c;

                            Button btn = new Button(categoria);
                            btn.getStyleClass().add("categoria-button");

                            btn.setOnAction(e -> {
                                // Si el botón clicado ya está seleccionado, lo deseleccionamos
                                if (botonSeleccionado == btn) {
                                    btn.getStyleClass().remove("selected");
                                    botonSeleccionado = null;
                                    cargarLibros(); // 🔁 cargar todos los libros
                                } else {
                                    // Si hay otro botón seleccionado, lo deseleccionamos
                                    if (botonSeleccionado != null) {
                                        botonSeleccionado.getStyleClass().remove("selected");
                                    }

                                    // Seleccionamos el nuevo botón
                                    btn.getStyleClass().add("selected");
                                    botonSeleccionado = btn;

                                    filtraPorCategoria(categoria); // 🔍 aplicar filtro
                                }
                            });

                            barraCategorias.getChildren().add(btn);

                        }
                    });

                } else {
                    System.out.println("Credenciales inválidas. Inténtalo de nuevo.");
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
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
                    Platform.runLater(() -> {
                        listalibros.getItems().clear();
                        for (Libro libro : libros) {
                            listalibros.getItems().add(libro);
                        }
                    });
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

    public void mostrarUsuarioLogueado(Usuario usuario) {
        lblNombreUsuario.setText(usuario.getNombre()+" "+usuario.getApellido() );
        lblNombreUsuario.setVisible(true);
        btnIniciarSesion.setVisible(false);
        btnCrearCuenta.setVisible(false);
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        Object source = event.getSource();

        if (source == btnIniciarSesion) {
            Stage stage = (Stage) btnIniciarSesion.getScene().getWindow();
            Utils.cambiarPantalla(stage, "/com/example/alexandriafrontend/Login.fxml", "/styles/Login.css", controller -> {});
            System.out.println("Cargando Login.fxml");
        } else if (source == btnCrearCuenta) {
            Stage stage = (Stage) btnCrearCuenta.getScene().getWindow();
            Utils.cambiarPantalla(stage, "/com/example/alexandriafrontend/Registro.fxml", "/styles/Registro.css", controller -> {});
            System.out.println("Cargando Registro.fxml");
        }
    }
}

