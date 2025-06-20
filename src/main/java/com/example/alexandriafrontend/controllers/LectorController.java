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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import org.fxmisc.richtext.StyleClassedTextArea;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LectorController {

    @FXML
    private AnchorPane lectorPane;

    @FXML
    private StyleClassedTextArea textArea;

    private final List<Anotacion> anotaciones = new CopyOnWriteArrayList<>();

    @FXML
    private HBox seccionAcciones;


    private Long libroId;

    private Long lecturaCompartidaId = null;

    @FXML
    private Button btnCompartir;

    private static final Map<Long, CacheEntry> cacheLibrosProcesados = new HashMap<>();
    private static final long CACHE_EXPIRATION_MS = 3600000; // 1 hora

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
        aplicarEstiloSeleccionado("");
    }

    @FXML
    private void initialize() {
        lectorPane.getStylesheets().add(getClass().getResource("/styles/lector.css").toExternalForm());
        textArea.getStylesheets().add(getClass().getResource("/styles/lector.css").toExternalForm());
        configurarTooltipComentarios();
        btnCompartir.setOnAction(e -> compartirLibroConUsuario());

        String token = SesionUsuario.getInstancia().getToken();
        if (token == null || token.trim().isEmpty()) {
            seccionAcciones.setVisible(false);
            seccionAcciones.setManaged(false); // evita que ocupe espacio
        }

        Logger.getLogger("nl.siegmann.epublib.epub.PackageDocumentReader").setLevel(Level.OFF);
        Logger.getLogger("nl.siegmann.epublib.epub.NCXDocument").setLevel(Level.OFF);
    }



    public void setIdLibro(Long libroId) { this.libroId = libroId; }

    public void setLecturaCompartidaId(Long lecturaCompartidaId) { this.lecturaCompartidaId = lecturaCompartidaId; }

/*

 private String leerYProcesarLibro(String urlFirmada) throws Exception {
        InputStream in = new URL(urlFirmada).openStream();
        Book book = new EpubReader().readEpub(in);


        StringBuilder sb = new StringBuilder();
        sb.append("[[TITULO]] ").append(book.getTitle().trim()).append("\n");
        sb.append("[[AUTOR]] ").append(book.getMetadata().getAuthors().stream()
                .map(a -> a.getFirstname() + " " + a.getLastname())
                .collect(Collectors.joining(", ")).trim()).append("\n\n");

        for (Resource res : book.getContents()) {
            String href = res.getHref().toLowerCase();
            if (href.contains("nav") || href.contains("toc") || href.contains("cover")) continue;

            String html = new String(res.getData(), StandardCharsets.UTF_8);

            // Paso clave 1: Procesar h2 manteniendo saltos de línea naturales
            html = html.replaceAll("(?is)<h2[^>]*>\\s*(.*?)\\s*</h2>", "[[H2]] $1 [[ENDH2]]");
            // Limpieza mejorada
            String text = html.replaceAll("(?is)<(script|style|title|h1|h4)[^>]*>.*?</\\1>", "")
                    .replaceAll("(?i)<[^>]+>", " ")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("\\s+", " ")
                    .replaceAll("(\\.|\\?|!)\\s*", "$1\n\n"); // Mantener saltos después de puntuación

            sb.append(text.trim()).append("\n\n");
        }

        return sb.toString().replaceAll("\\n{3,}", "\n\n");
    }
 */



    /**
     * Muestra el texto procesado en el StyleClassedTextArea,
     * aplicando estilos a título, autor y contenido.
     */
    private void mostrarTextoEnArea(String texto) {
        Platform.runLater(() -> {
            // Limpieza inicial
            textArea.clear();
            textArea.setEditable(false);

            // 1. Carga todo el texto de una vez (más eficiente que append)
            textArea.replaceText(texto);

            // 2. Aplicar estilos por secciones
            aplicarEstilosGlobales(texto);

            // Posicionamiento inicial
            textArea.moveTo(0);
            textArea.requestFollowCaret();
        });
    }

    private void aplicarEstilosGlobales(String textoOriginal) {
        StringBuilder textoLimpio = new StringBuilder();

        // Listas para guardar posiciones y estilos
        List<Integer> inicios = new ArrayList<>();
        List<Integer> fines = new ArrayList<>();
        List<String> clases = new ArrayList<>();

        for (String linea : textoOriginal.split("\n")) {
            if (linea.startsWith("[[TITULO]]")) {
                String contenido = linea.replace("[[TITULO]]", "").trim();
                int inicio = textoLimpio.length();
                textoLimpio.append(contenido).append("\n");
                int fin = textoLimpio.length();
                inicios.add(inicio);
                fines.add(fin);
                clases.add("titulo");

            } else if (linea.startsWith("[[AUTOR]]")) {
                String contenido = linea.replace("[[AUTOR]]", "").trim();
                int inicio = textoLimpio.length();
                textoLimpio.append("\n").append(contenido).append("\n\n");
                int fin = textoLimpio.length();
                inicios.add(inicio);
                fines.add(fin);
                clases.add("autor");

            } else if (linea.contains("[[H2]]") && linea.contains("[[ENDH2]]")) {
                int inicioEtiqueta = linea.indexOf("[[H2]]") + 6;
                int finEtiqueta = linea.indexOf("[[ENDH2]]");
                String subtitulo = linea.substring(inicioEtiqueta, finEtiqueta).trim();
                String resto = linea.substring(finEtiqueta + 9).trim();

                // Subtítulo
                int inicio = textoLimpio.length();
                textoLimpio.append(subtitulo).append("\n");
                int fin = textoLimpio.length();
                inicios.add(inicio);
                fines.add(fin);
                clases.add("subtitulo");

                // Texto restante
                if (!resto.isEmpty()) {
                    int inicioTexto = textoLimpio.length();
                    textoLimpio.append(resto).append("\n");
                    int finTexto = textoLimpio.length();
                    inicios.add(inicioTexto);
                    fines.add(finTexto);
                    clases.add("lector-area");
                }

            } else {
                // Texto normal
                String contenido = linea.trim();
                if (!contenido.isEmpty()) {
                    int inicio = textoLimpio.length();
                    textoLimpio.append(contenido).append("\n");
                    int fin = textoLimpio.length();
                    inicios.add(inicio);
                    fines.add(fin);
                    clases.add("lector-area");
                }
            }
        }

        // 1. Reemplazar todo el contenido con texto limpio
        textArea.replaceText(textoLimpio.toString());

        // 2. Aplicar estilos a los rangos correctos
        for (int i = 0; i < inicios.size(); i++) {
            textArea.setStyleClass(inicios.get(i), fines.get(i), clases.get(i));
        }
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

    // Variable de clase (añadir al inicio de LectorController)
    public void cargarLibroDesdeURL(String urlFirmada) {
        // 1. Verificar cache válido
        CacheEntry cached = cacheLibrosProcesados.get(libroId);
        if (cached != null && cached.isValid()) {
            Platform.runLater(() -> {
                mostrarTextoEnArea(cached.contenido);
                cargarAnotaciones();
            });
            return;
        }

        // 2. Procesamiento en segundo plano con mejor manejo de recursos
        Task<String> processingTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                try (InputStream in = new URL(urlFirmada).openStream()) {
                    return procesarLibroEficientemente(in);
                }
            }
        };

        processingTask.setOnSucceeded(e -> {
            String resultado = processingTask.getValue();
            cacheLibrosProcesados.put(libroId, new CacheEntry(resultado));
            mostrarTextoEnArea(resultado);
            cargarAnotaciones();
        });

        processingTask.setOnFailed(e -> {
            mostrarErrorCarga();
            processingTask.getException().printStackTrace();
        });

        new Thread(processingTask, "LibroProcessingThread").start();
    }

    private String procesarLibroEficientemente(InputStream in) throws IOException {
        Book book = new EpubReader().readEpub(in);
        StringBuilder sb = new StringBuilder(10000); // Tamaño inicial estimado

        // Encabezado optimizado
        sb.append("[[TITULO]] ").append(book.getTitle().trim()).append('\n')
                .append("[[AUTOR]] ").append(book.getMetadata().getAuthors().stream()
                        .map(a -> a.getFirstname() + " " + a.getLastname())
                        .collect(Collectors.joining(", ")).trim()).append("\n\n");

        // Procesamiento paralelo de recursos (Java 8+)
        book.getContents().parallelStream()
                .filter(res -> !res.getHref().toLowerCase().matches(".*(nav|toc|cover).*"))
                .forEach(res -> procesarRecurso(res, sb));

        return sb.toString();
    }

    private void procesarRecurso(Resource res, StringBuilder sb) {
        try {
            String html = new String(res.getData(), StandardCharsets.UTF_8);
            String processed = html.replaceAll("(?is)<h2[^>]*>\\s*(.*?)\\s*</h2>", "[[H2]] $1 [[ENDH2]]")
                    .replaceAll("(?is)<(script|style|title|h1|h4)[^>]*>.*?</\\1>", "")
                    .replaceAll("<[^>]+>", " ")
                    .replaceAll("\\s+", " ")
                    .replaceAll("([.!?])\\s+", "$1\n\n")
                    .trim();

            synchronized (sb) {
                sb.append(processed).append("\n\n");
            }
        } catch (Exception e) {
            System.err.println("Error procesando recurso: " + res.getHref());
        }
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
        dialogo.setGraphic(null);
        dialogo.setTitle("Comentario");
        dialogo.setHeaderText("Escribe el comentario del subrayado");
        dialogo.setContentText("Comentario:");


        URL cssUrl = getClass().getResource("/styles/alertas.css");
        if (cssUrl != null) {
            dialogo.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("No se encontró /styles/alert.css");
        }


        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/image/logo.png")));
        logo.setFitWidth(48);
        logo.setFitHeight(48);
        dialogo.setGraphic(logo);


        Optional<String> resultado = dialogo.showAndWait();

        resultado.ifPresent(comentario -> {
            for (int i = start; i < end; i++) {
                List<String> estilos = new ArrayList<>(textArea.getStyleOfChar(i));
                if (!estilos.contains("comentado")) estilos.add("comentado");
                textArea.setStyle(i, i + 1, estilos);
            }

            List<String> estilos = new ArrayList<>(textArea.getStyleOfChar(start));
            anotaciones.add(new Anotacion(start, end, estilos, comentario));
        });
    }




    private void configurarTooltipComentarios() {
        Tooltip tooltip = new Tooltip();
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);

        textArea.setOnMouseMoved(event -> {
            Platform.runLater(() -> {
                int pos;
                try {
                    pos = textArea.hit(event.getX(), event.getY()).getInsertionIndex();
                } catch (Exception e) {
                    // Si ocurre ConcurrentModification, ignoramos y salimos (JavaFX volverá a intentarlo)
                    return;
                }

                // Hacemos una copia inmutable de la lista en este momento
                List<Anotacion> anotacionesSnapshot = List.copyOf(anotaciones);

                for (Anotacion a : anotacionesSnapshot) {
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
        });

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

                        });
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



    private static class CacheEntry {
        String contenido;
        long timestamp;

        CacheEntry(String contenido) {
            this.contenido = contenido;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRATION_MS;
        }
    }
}