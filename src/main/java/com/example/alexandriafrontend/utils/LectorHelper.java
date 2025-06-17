package com.example.alexandriafrontend.utils;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.controllers.LectorController;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.session.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LectorHelper {

    private static final ApiService apiService = ApiClient.getApiService();
    private static final Map<Long, String[]> cacheLibros = new HashMap<>();

    // Método SIN modificaciones (se mantiene igual)
    private static final long CACHE_EXPIRATION_TIME_MS = 3600000; // 1 hora

    public static void obtenerArchivoUrlPorId(Long idLibro, Consumer<String> callback) {
        // Verificar cache primero
        if (cacheLibros.containsKey(idLibro) ){
            String[] cache = cacheLibros.get(idLibro);
            if (cache[0] != null) {
                callback.accept(cache[0]);
                return;
            }
        }

        Call<ResponseBody> call = apiService.obtenerArchivoUrl(idLibro);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String archivoNombre = response.body().string();
                        // Actualizar cache
                        String[] cache = cacheLibros.getOrDefault(idLibro, new String[2]);
                        cache[0] = archivoNombre;
                        cacheLibros.put(idLibro, cache);
                        callback.accept(archivoNombre);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.accept(null);
                    }
                } else {
                    System.out.println("No se pudo obtener el nombre del archivo desde el backend.");
                    callback.accept(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.accept(null);
            }
        });
    }

    // Método SIN modificaciones (se mantiene igual)
    public static void obtenerUrlFirmada(String archivoNombre, Consumer<String> callback) {
        Call<ResponseBody> call = apiService.obtenerUrlFirmada(archivoNombre);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String url = response.body().string();
                        callback.accept(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.accept(null);
                    }
                } else {
                    System.out.println("No se pudo obtener la URL firmada.");
                    callback.accept(null);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.accept(null);
            }
        });
    }

    // Método optimizado - Versión combinada para evitar llamadas secuenciales
    public static void pedirUrlYMostrarLibro(Libro libro, AnchorPane contenido) {
        // 1. Verificar cache
        if (cacheLibros.containsKey(libro.getId())) {
            String[] cache = cacheLibros.get(libro.getId());
            if (cache[1] != null) { // Si tenemos URL firmada
                cargarLectorDirectamente(cache[1], libro.getId(), contenido);
                registrarLecturaAsync(libro.getId());
                return;
            } else if (cache[0] != null) { // Si solo tenemos nombre archivo
                obtenerUrlFirmadaYActualizarCache(cache[0], libro.getId(), contenido);
                registrarLecturaAsync(libro.getId());
                return;
            }
        }

        // 2. Registrar lectura y obtener datos
        registrarLecturaAsync(libro.getId());
        obtenerArchivoUrlPorId(libro.getId(), archivoNombre -> {
            if (archivoNombre != null) {
                obtenerUrlFirmadaYActualizarCache(archivoNombre, libro.getId(), contenido);
            } else {
                Platform.runLater(() ->
                        Utils.mostrarMensaje("No se pudo obtener el archivo del libro."));
            }
        });
    }

    private static void obtenerUrlFirmadaYActualizarCache(String archivoNombre, Long libroId, AnchorPane contenido) {
        obtenerUrlFirmada(archivoNombre, urlFirmada -> {
            if (urlFirmada != null) {
                // Actualizar cache
                String[] cache = cacheLibros.getOrDefault(libroId, new String[2]);
                cache[0] = archivoNombre;
                cache[1] = urlFirmada;
                cacheLibros.put(libroId, cache);

                cargarLectorDirectamente(urlFirmada, libroId, contenido);
            } else {
                Platform.runLater(() ->
                        Utils.mostrarMensaje("No se pudo obtener la URL firmada."));
            }
        });
    }

    private static void registrarLecturaAsync(Long libroId) {
        String token = SesionUsuario.getInstancia().getToken();
        if (token != null) {
            new Thread(() -> {
                try {
                    apiService.registrarLectura("Bearer " + token, libroId).execute();
                    System.out.println("Lectura registrada en segundo plano");
                } catch (IOException e) {
                    System.err.println("Error al registrar lectura en segundo plano");
                }
            }).start();
        }
    }


    // Nuevo método auxiliar para evitar duplicación de código
    private static void cargarLectorDirectamente(String urlFirmada, Long libroId, AnchorPane contenido) {
        Platform.runLater(() -> {
            cargarPantalla(contenido, "/com/example/alexandriafrontend/Lector.fxml",
                    (LectorController controller) -> {
                        controller.setIdLibro(libroId);
                        controller.cargarLibroDesdeURL(urlFirmada);
                    }
            );
        });
    }

    // Método SIN modificaciones (se mantiene igual)
    public static void pedirUrlYMostrarLibroColaborativo(Libro libro, Long lecturaCompartidaId, AnchorPane contenido) {
        obtenerArchivoUrlPorId(libro.getId(), archivoNombre -> {
            if (archivoNombre != null) {
                obtenerUrlFirmada(archivoNombre, urlFirmada -> {
                    if (urlFirmada != null) {
                        Platform.runLater(() -> {
                            cargarPantalla(contenido, "/com/example/alexandriafrontend/Lector.fxml",
                                    (LectorController controller) -> {
                                        controller.setIdLibro(libro.getId());
                                        controller.setLecturaCompartidaId(lecturaCompartidaId);
                                        controller.cargarLibroDesdeURL(urlFirmada);
                                    });
                        });
                    } else {
                        Utils.mostrarMensaje("No se pudo obtener la URL firmada para el libro colaborativo.");
                    }
                });
            } else {
                Utils.mostrarMensaje("No se pudo obtener el nombre del archivo para el libro colaborativo.");
            }
        });
    }

    // Método SIN modificaciones (se mantiene igual)
    public static <T> void cargarPantalla(AnchorPane contenedor, String rutaFXML, Consumer<T> logicaControlador) {
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(rutaFXML));
            AnchorPane nuevoContenido = loader.load();
            T controller = loader.getController();
            logicaControlador.accept(controller);

            contenedor.getChildren().clear();
            contenedor.getChildren().add(nuevoContenido);
            AnchorPane.setTopAnchor(nuevoContenido, 0.0);
            AnchorPane.setBottomAnchor(nuevoContenido, 0.0);
            AnchorPane.setLeftAnchor(nuevoContenido, 0.0);
            AnchorPane.setRightAnchor(nuevoContenido, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}