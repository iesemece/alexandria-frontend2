package com.example.alexandriafrontend.controllers;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.model.Anotacion;
import com.example.alexandriafrontend.request.AnotacionesRequest;
import com.example.alexandriafrontend.session.SesionUsuario;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    ApiService apiService = ApiClient.getApiService();

    public void setIdLibro(Long libroId) {
        this.libroId = libroId;
    }

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
    }

    private String leerYProcesarLibro(String urlFirmada) throws Exception {
        InputStream inputStream = new URL(urlFirmada).openStream();
        Book libro = new EpubReader().readEpub(inputStream);

        String titulo = libro.getTitle();
        String autor = libro.getMetadata().getAuthors().stream()
                .map(a -> a.getFirstname() + " " + a.getLastname())
                .collect(Collectors.joining(", "));

        StringBuilder textoFinal = new StringBuilder();
        textoFinal.append("[[TITULO]] ").append(titulo.trim()).append("\n");
        textoFinal.append("[[AUTOR]] ").append(autor.trim()).append("\n\n");

        for (Resource recurso : libro.getContents()) {
            byte[] data = recurso.getData();
            if (data != null && data.length > 0) {
                String texto = new String(data, StandardCharsets.UTF_8);

                // Eliminar basura HTML
                texto = texto.replaceAll("(?is)<style[^>]*>.*?</style>", "");
                texto = texto.replaceAll("(?is)<script[^>]*>.*?</script>", "");
                texto = texto.replaceAll("(?is)<title[^>]*>.*?</title>", "");
                texto = texto.replaceAll("(?i)<[^>]+>", "");
                texto = texto.replaceAll("&nbsp;", " ");

                // Filtrado de líneas basura
                if (texto.toLowerCase().contains("produced by calibre")
                        || texto.toLowerCase().contains("sobrecubierta")
                        || texto.toLowerCase().contains("bookdesigner")
                        || texto.toLowerCase().contains("tags:")
                        || texto.toLowerCase().contains("editorial")
                        || texto.toLowerCase().contains("isbn")
                        || texto.toLowerCase().contains("copyright")) {
                    continue;
                }

                // Normalizar saltos de línea y espacios
                texto = texto.replaceAll("\\s{3,}", " ");
                texto = texto.replaceAll("\\n{3,}", "\n\n");

                // Separar en párrafos tras puntos
                texto = texto.replaceAll("\\.\\s*", ".\n\n");

                textoFinal.append(texto.trim()).append("\n\n");
            }
        }

        return textoFinal.toString().trim();
    }


    private void mostrarTextoEnArea(String texto) {
        textArea.clear();
        textArea.setEditable(false);
        textArea.moveTo(0);

        String[] lineas = texto.split("\n");
        int cursor = 0;

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            String contenido = "";
            if (linea.startsWith("[[TITULO]]")) {
                contenido = linea.replace("[[TITULO]]", "").trim() + "\n";
                textArea.appendText(contenido);
                textArea.setStyleClass(cursor, cursor + contenido.length(), "titulo");
            } else if (linea.startsWith("[[AUTOR]]")) {
                contenido = linea.replace("[[AUTOR]]", "").trim() + "\n\n";
                textArea.appendText(contenido);
                textArea.setStyleClass(cursor, cursor + contenido.length(), "autor");
            } else {
                contenido = linea + "\n\n";
                textArea.appendText(contenido);
                textArea.setStyleClass(cursor, cursor + contenido.length(), "lector-area");
            }

            cursor += contenido.length();
        }

        textArea.moveTo(0);
        textArea.requestFollowCaret();
    }
    //textArea.requestFollowCaret();

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

        String token = SesionUsuario.getInstancia().getToken(); // 👈 así lo coges
        if (libroId == null || token == null) {
            System.out.println("No se puede guardar: libroId o token nulo");
            return;
        }

        AnotacionesRequest request = new AnotacionesRequest();

        request.setLibroId(libroId);

        Map<Integer, List<Anotacion>> mapa = new HashMap<>();
        mapa.put(0, new ArrayList<>(anotaciones)); // lista ya generada con tus anotaciones
        request.setAnotaciones(mapa);

        Call<Void> call = apiService.guardarAnotaciones("Bearer " + token ,request);

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

    public void cargarAnotaciones() {
        String token = SesionUsuario.getInstancia().getToken();
        if (libroId == null || token == null) {
            System.out.println("No se pueden cargar anotaciones: libroId o token nulo");
            return;
        }

        Call<Map<Integer, List<Anotacion>>> call = apiService.obtenerAnotaciones("Bearer " + token, libroId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<Integer, List<Anotacion>>> call, Response<Map<Integer, List<Anotacion>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<Integer, List<Anotacion>> mapa = response.body();
                    List<Anotacion> anotacionesRecibidas = mapa.getOrDefault(0, new ArrayList<>()); // página 0

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