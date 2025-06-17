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

    private static final Map<String, String> TITULO_A_CATEGORIA = Map.ofEntries(
            Map.entry("1984", "Ciencia Ficción"),
            Map.entry("Apocalipsis Z - Los dias oscuros", "Suspense"),
            Map.entry("De ratones y hombres", "Drama"),
            Map.entry("El árbol", "Ciencia Ficción"),
            Map.entry("El caballero de la armadura oxidada", "Fantasía"),
            Map.entry("El camino", "Drama"),
            Map.entry("El círculo cero", "Ciencia Ficción"),
            Map.entry("El Extraño", "Ciencia Ficción"),
            Map.entry("El Hobbit", "Fantasía"),
            Map.entry("El hombre de los círculos azules", "Suspense"),
            Map.entry("El laberinto griego", "Suspense"),
            Map.entry("El pesar de Odín el Godo", "Ciencia Ficción"),
            Map.entry("El Principito", "Fantasía"),
            Map.entry("El secreto de la porcelana", "Suspense"),
            Map.entry("El terrible anciano", "Ciencia Ficción"),
            Map.entry("El Túnel del Tiempo", "Ciencia Ficción"),
            Map.entry("El último gran amor", "Drama"),
            Map.entry("En el sótano", "Suspense"),
            Map.entry("Fahrenheit 451", "Ciencia Ficción"),
            Map.entry("Hombres y dragones", "Fantasía"),
            Map.entry("Juana la Loca", "Romance"),
            Map.entry("La horda amarilla", "Suspense"),
            Map.entry("La isla", "Suspense"),
            Map.entry("La marca del lobo", "Romance"),
            Map.entry("La princesa de Eboli", "Romance"),
            Map.entry("La Rueda del Cielo", "Ciencia Ficción"),
            Map.entry("Los hombres de venus", "Ciencia Ficción"),
            Map.entry("Los muertos no caminan y otros cuentos", "Suspense"),
            Map.entry("Los otros dioses", "Ciencia Ficción"),
            Map.entry("Mis enigmas", "Suspense"),
            Map.entry("Mundo de Tinieblas - Vampiro", "Fantasía"),
            Map.entry("Otelo", "Drama"),
            Map.entry("Poesías", "Romance"),
            Map.entry("Rebelión en la granja", "Suspense"),
            Map.entry("Riesgo mortal", "Suspense"),
            Map.entry("Sherlock Holmes 10 - El archivo de Sherlock Holmes", "Suspense")
    );



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

                        // Busca los nodos del FXML
                        ImageView imgCategoria = (ImageView) pane.lookup("#imgCategoria");
                        Label lblTitulo = (Label) pane.lookup("#lblTitulo");
                        Label lblAutor = (Label) pane.lookup("#lblAutor");
                        Label lblCategoria = (Label) pane.lookup("#lblCategoria");

                        lblTitulo.setText(libro.getTitulo());
                        lblAutor.setText(libro.getAutor());
                        String categoria = obtenerCategoriaPorTitulo(libro.getTitulo());
                        lblCategoria.setText(categoria);

                        // Imagen: solo carpeta /image/
                        String nombreBase = categoria.toLowerCase().replace(" ", "_");
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
    }

        private void cargarLibros() {
        Call<List<Libro>> call = apiService.obtenerTodosLibros();
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Libro libro : response.body()) {
                        listalibros.getItems().clear();
                        Libro nuevoLibro = new Libro(libro.getId(), libro.getTitulo(), libro.getAutor());
                        javafx.application.Platform.runLater(() -> listalibros.getItems().add(nuevoLibro));
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
                    for (Libro libro : response.body()) {
                        listalibros.getItems().clear();
                        Libro nuevoLibro = new Libro(libro.getId(), libro.getTitulo(), libro.getAutor());
                        javafx.application.Platform.runLater(() -> listalibros.getItems().add(nuevoLibro));
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



    private String obtenerCategoriaPorTitulo(String titulo) {
        return TITULO_A_CATEGORIA.getOrDefault(titulo, "Ciencia Ficción");
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

