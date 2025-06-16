package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Anotacion;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.request.AnotacionesRequest;
import com.example.alexandriafrontend.session.SesionUsuario;
import com.example.alexandriafrontend.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import org.fxmisc.richtext.StyleClassedTextArea;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class LectorController {

    @FXML
    private AnchorPane lectorPane;

    @FXML
    private StyleClassedTextArea textArea;

    private final List<Anotacion> anotaciones = new ArrayList<>();

    private Long libroId;

    private Long lecturaCompartidaId = null;

    @FXML
    private Button btnCompartir;


    ApiService apiService = ApiClient.getApiService();


    @FXML
    private void subrayarAmarillo() {
        aplicarEstiloSeleccionado("color-amarillo");
    }

    @FXML
    private void subrayarAzul() {
        aplicarEstiloSeleccionado("color-azul");
    }

    @FXML
    private void subrayarVerde() {
        aplicarEstiloSeleccionado("color-verde");
    }

    @FXML
    private void quitarSubrayado() {
        aplicarEstiloSeleccionado(""); // Sin clase = estilo por defecto
    }

    @FXML
    private void initialize() {
        textArea.getStylesheets().add(getClass().getResource("/styles/lector.css").toExternalForm());
        configurarTooltipComentarios();
        btnCompartir.setOnAction(e -> compartirLibroConUsuario());
    }



    public void setIdLibro(Long libroId) { this.libroId = libroId; }

    public void setLecturaCompartidaId(Long lecturaCompartidaId) { this.lecturaCompartidaId = lecturaCompartidaId; }



    private String leerYProcesarLibro(String urlFirmada) throws Exception {
        InputStream in = new URL(urlFirmada).openStream();
        Book book = new EpubReader().readEpub(in);

        // Construir encabezado con título y autor
        String title = book.getTitle();
        String author = book.getMetadata().getAuthors().stream()
                .map(a -> a.getFirstname() + " " + a.getLastname())
                .collect(Collectors.joining(", "));

        StringBuilder sb = new StringBuilder();
        sb.append("[[TITULO]] ").append(title.trim()).append("\n");
        sb.append("[[AUTOR]] ").append(author.trim()).append("\n");

        // Recorrer capítulos y secciones
        for (Resource res : book.getContents()) {
            String href = res.getHref().toLowerCase();
            if (href.contains("nav") || href.contains("toc") || href.contains("cover")) continue;

            String html = new String(res.getData(), StandardCharsets.UTF_8);

            // Detectar <h2> y marcarlos como [[CAPITULO]]
            html = html.replaceAll("(?is)<h2[^>]*>\\s*(.*?)\\s*</h2>", "\n[[H2]] $1\n");

            // Limpieza básica de HTML
            String text = html.replaceAll("(?is)<(script|style|title|h1|h4)[^>]*>.*?</\\1>", "")
                    .replaceAll("(?i)<[^>]+>", "")
                    .replaceAll("&nbsp;", " ");

            // Filtrar líneas irrelevantes
            if (text.matches("(?i).*\\b(isbn|copyright|editorial|produced by calibre|bookdesigner|tags|sobrecubierta)\\b.*")) continue;

            // Normalizar espacios y párrafos
            text = text.replaceAll("\\s+", " ")
                    .replaceAll("\\.\\s*", ".\n\n");

            sb.append(text.trim()).append("\n\n");
        }
        return sb.toString().trim();
    }


    /**
     * Muestra el texto procesado en el StyleClassedTextArea,
     * aplicando estilos a título, autor y contenido.
     */
    private void mostrarTextoEnArea(String texto) {
        Platform.runLater(() -> {
            textArea.clear();
            textArea.setEditable(false);

            int pos = 0;
            for (String line : texto.split("\n")) {
                if (line.startsWith("[[TITULO]]")) {
                    String t = line.replace("[[TITULO]]", "").trim() + "\n";
                    textArea.appendText(t);
                    textArea.setStyleClass(pos, pos + t.length(), "titulo");
                    pos += t.length();
                } else if (line.startsWith("[[AUTOR]]")) {
                    String a = line.replace("[[AUTOR]]", "").trim() + "\n";
                    textArea.appendText(a);
                    textArea.setStyleClass(pos, pos + a.length(), "autor");
                    pos += a.length();
                } else if (line.startsWith("[[CAPITULO]]")) {
                    String cap = line.replace("[[CAPITULO]]", "").trim() + "\n\n";
                    textArea.appendText(cap);
                    textArea.setStyleClass(pos, pos + cap.length(), "capitulo");
                    pos += cap.length();
                } else {
                    String c = line.trim() + "\n";
                    textArea.appendText(c);
                    textArea.setStyleClass(pos, pos + c.length(), "lector-area");
                    pos += c.length();
                }
            }

            textArea.moveTo(0);
            textArea.requestFollowCaret();
        });
    }


    //textArea.requestFollowCaret();

    private void compartirLibroConUsuario() {
        String token = SesionUsuario.getInstancia().getToken();
        Long usuarioId = SesionUsuario.getInstancia().getIdUsuario();

        if (libroId == null || token == null || usuarioId == null) {
            System.out.println("Faltan datos para compartir libro");
            return;
        }

        // 1. Pedimos la lista de usuarios disponibles
        ApiService apiService = ApiClient.getApiService();
        Call<List<UsuarioListado>> call = apiService.obtenerUsuarios("Bearer " + token);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<UsuarioListado>> call, Response<List<UsuarioListado>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Platform.runLater(() -> {
                        List<UsuarioListado> usuarios = response.body();
                        if (usuarios.isEmpty()) {
                            Utils.mostrarMensaje("No hay otros usuarios disponibles.");
                            return;
                        }

                        // 2. Mostrar diálogo para elegir usuario
                        ChoiceDialog<UsuarioListado> dialog = new ChoiceDialog<>(usuarios.get(0), usuarios);
                        dialog.setTitle("Compartir libro");
                        dialog.setHeaderText("Selecciona el usuario para compartir el libro:");
                        dialog.setContentText("Usuario:");

                        Optional<UsuarioListado> resultado = dialog.showAndWait();
                        resultado.ifPresent(usuarioDestino -> {
                            // 3. Llamada para compartir el libro
                            compartirLibro(usuarioId, usuarioDestino.getId(), libroId, token);
                        });
                    });
                } else {
                    Utils.mostrarMensaje("Error cargando usuarios para compartir.");
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioListado>> call, Throwable t) {
                Platform.runLater(() -> Utils.mostrarMensaje("Error al conectar para compartir."));
            }
        });
    }


    private void compartirLibro(Long usuarioId, Long usuarioDestinoId, Long libroId, String token) {
        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.compartirLibro(
                usuarioId,
                usuarioDestinoId,
                libroId,
                "Bearer " + token
        );
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        Utils.mostrarMensaje("Libro compartido correctamente.");
                    } else {
                        Utils.mostrarMensaje("Error al compartir libro: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Platform.runLater(() -> Utils.mostrarMensaje("Fallo de red al compartir libro."));
            }
        });
    }



    private void mostrarErrorCarga() {
        textArea.clear();
        textArea.replaceText("Error al cargar el libro.");
    }

    public void cargarLibroDesdeURL(String urlFirmada) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String textoPlano = leerYProcesarLibro(urlFirmada);
                Platform.runLater(() -> mostrarTextoEnArea(textoPlano));
                cargarAnotaciones();
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> mostrarErrorCarga());
                getException().printStackTrace();
            }
        };
        new Thread(task).start();
    }

    private void aplicarEstiloSeleccionado(String colorClaseCss) {
        int start = textArea.getSelection().getStart();
        int end = textArea.getSelection().getEnd();

        for (int i = start; i < end; i++) {
            var estilosActuales = textArea.getStyleOfChar(i);
            List<String> nuevosEstilos = new ArrayList<>(estilosActuales);

            // Quitar cualquier subrayado previo
            nuevosEstilos.removeIf(estilo -> estilo.startsWith("color-"));

            // Añadir nuevo color
            if (!colorClaseCss.isEmpty()) {
                nuevosEstilos.add(colorClaseCss);
            }

            textArea.setStyle(i, i + 1, nuevosEstilos);
        }

        List<String> estilos = new ArrayList<>(textArea.getStyleOfChar(start));
        Anotacion subrayadoSimple = new Anotacion(start, end, estilos, null);

        anotaciones.add(subrayadoSimple);
    }


    @FXML
    private void agregarComentario() {
        int start = textArea.getSelection().getStart();
        int end = textArea.getSelection().getEnd();

        if (start == end) {
            System.out.println("No hay texto seleccionado.");
            return;
        }

        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Nuevo comentario");
        dialogo.setHeaderText("Introduce el comentario asociado al subrayado");
        dialogo.setContentText("Comentario: ");

        Optional<String> resultado = dialogo.showAndWait();

        resultado.ifPresent(comentario -> {
            for (int i = start; i < end; i++) {
                List<String> estilos = new ArrayList<>(textArea.getStyleOfChar(i));

                if (!estilos.contains("comentado")) {
                    estilos.add("comentado");  // línea decorativa
                }

                textArea.setStyle(i, i + 1, estilos);
            }

            List<String> estilos = new ArrayList<>(textArea.getStyleOfChar(start));
            Anotacion nueva = new Anotacion(start, end, estilos, comentario);
            anotaciones.add(nueva);
        });

    }


    private void configurarTooltipComentarios() {
        Tooltip tooltip = new Tooltip();
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);

        textArea.setOnMouseMoved(event -> {
            int pos = textArea.hit(event.getX(), event.getY()).getInsertionIndex();

            for (Anotacion a : anotaciones) {
                if (a.getComentario() != null && pos >= a.getStart() && pos <= a.getEnd()) {
                    if (!tooltip.isShowing()) {
                        tooltip.setText(a.getComentario());
                        tooltip.show(textArea, event.getScreenX() + 10, event.getScreenY() + 10);
                    }
                    return;
                }
            }

            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        });

        // Oculta tooltip si el ratón sale del área
        textArea.setOnMouseExited(event -> {
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        });
    }

    @FXML
    private void guardarAnotaciones() {
        String token = SesionUsuario.getInstancia().getToken();
        if (libroId == null || token == null) {
            System.out.println("No se puede guardar: libroId o token nulo");
            return;
        }

        AnotacionesRequest request = new AnotacionesRequest();
        request.setLibroId(libroId);

        Map<Integer, List<Anotacion>> mapa = new HashMap<>();
        mapa.put(0, new ArrayList<>(anotaciones));
        request.setAnotaciones(mapa);


        if (lecturaCompartidaId != null) {
            // Guardar anotaciones colaborativas
            Call<Void> call = apiService.guardarAnotacionesCompartidas(
                    lecturaCompartidaId,
                    request
            );
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        System.out.println("Anotaciones colaborativas guardadas con éxito");
                    } else {
                        System.out.println("Error al guardar anotaciones colaborativas: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    System.out.println("Fallo en la conexión al guardar anotaciones colaborativas");
                }
            });
        } else {
            // Guardar anotaciones normales
            Call<Void> call = apiService.guardarAnotaciones("Bearer " + token, request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        System.out.println("Anotaciones guardadas con éxito");
                    } else {
                        System.out.println("Error al guardar anotaciones: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    t.printStackTrace();
                    System.out.println("Fallo en la conexión al guardar anotaciones");
                }
            });
        }
    }

    public void cargarAnotaciones() {
        String token = SesionUsuario.getInstancia().getToken();
        if (libroId == null || token == null) {
            System.out.println("No se pueden cargar anotaciones: libroId o token nulo");
            return;
        }
        if (lecturaCompartidaId != null) {
            // Cargar anotaciones colaborativas
            Call<Map<Integer, List<Anotacion>>> call = apiService.obtenerAnotacionesCompartidas(
                    lecturaCompartidaId
            );
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Map<Integer, List<Anotacion>>> call, Response<Map<Integer, List<Anotacion>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<Integer, List<Anotacion>> mapa = response.body();
                        List<Anotacion> anotacionesRecibidas = mapa.getOrDefault(0, new ArrayList<>());
                        Platform.runLater(() -> {
                            for (Anotacion a : anotacionesRecibidas) {
                                for (int i = a.getStart(); i < a.getEnd(); i++) {
                                    textArea.setStyle(i, i + 1, a.getEstilos());
                                }
                                anotaciones.add(a);
                            }
                            System.out.println("📝 Anotaciones colaborativas cargadas y aplicadas.");
                        });
                    } else {
                        System.out.println("No se encontraron anotaciones colaborativas o error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<Integer, List<Anotacion>>> call, Throwable t) {
                    t.printStackTrace();
                    System.out.println("Fallo al conectar para recuperar anotaciones colaborativas.");
                }
            });
        } else {
            // Cargar anotaciones normales
            Call<Map<Integer, List<Anotacion>>> call = apiService.obtenerAnotaciones("Bearer " + token, libroId);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Map<Integer, List<Anotacion>>> call, Response<Map<Integer, List<Anotacion>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<Integer, List<Anotacion>> mapa = response.body();
                        List<Anotacion> anotacionesRecibidas = mapa.getOrDefault(0, new ArrayList<>());
                        Platform.runLater(() -> {
                            for (Anotacion a : anotacionesRecibidas) {
                                for (int i = a.getStart(); i < a.getEnd(); i++) {
                                    textArea.setStyle(i, i + 1, a.getEstilos());
                                }
                                anotaciones.add(a);
                            }
                            System.out.println("📝 Anotaciones cargadas y aplicadas.");
                        });
                    } else {
                        System.out.println("No se encontraron anotaciones o error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Map<Integer, List<Anotacion>>> call, Throwable t) {
                    t.printStackTrace();
                    System.out.println("Fallo al conectar para recuperar anotaciones.");
                }
            });
        }
    }

    @FXML
    private void anadirFavoritos() {
        String tokend = SesionUsuario.getInstancia().getToken();
        if (tokend != null && !tokend.trim().isEmpty()) {
            apiService.registrarFavoritos("Bearer " + SesionUsuario.getInstancia().getToken(), libroId).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    System.out.println("Lectura registrada correctamente.");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.err.println("Error al registrar lectura:");
                    t.printStackTrace();
                }
            });
        }

    }
}