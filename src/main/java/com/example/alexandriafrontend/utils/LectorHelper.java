package com.example.alexandriafrontend.utils;

import com.example.alexandriafrontend.api.ApiClient;
import com.example.alexandriafrontend.api.ApiService;
import com.example.alexandriafrontend.controllers.LectorController;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.session.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.Consumer;

public class LectorHelper {

    private static final ApiService apiService = ApiClient.getApiService();

    public static void obtenerArchivoUrlPorId(Long idLibro, Consumer<String> callback) {
        ApiService apiService = ApiClient.getApiService();
        Call<ResponseBody > call = apiService.obtenerArchivoUrl(idLibro);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String archivoNombre = response.body().string();
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

    public static void obtenerUrlFirmada(String archivoNombre, Consumer<String> callback) {
        Call<ResponseBody> call = apiService.obtenerUrlFirmada(archivoNombre);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String url = response.body().string(); // 🔥 aquí obtienes el string sin necesidad de Gson
                        callback.accept(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                        callback.accept(null);
                    }
                } else {
                    System.out.println("No se pudo obtener la URL firmada.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static <T> void cargarPantalla(AnchorPane contenedor, String rutaFXML, Consumer<T> logicaControlador) {
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(rutaFXML));
            AnchorPane nuevoContenido = loader.load();
            T controller = loader.getController();
            logicaControlador.accept(controller); // Aquí le pasas lógica al controller

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


    public static void pedirUrlYMostrarLibro(Libro libro, AnchorPane contenido) {

        apiService.registrarLectura("Bearer " + SesionUsuario.getInstancia().getToken(), libro.getId()).enqueue(new Callback<>() {
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

        obtenerArchivoUrlPorId(libro.getId(), archivoNombre -> {
            if (archivoNombre != null) {
                obtenerUrlFirmada(archivoNombre, urlFirmada -> {
                    if (urlFirmada != null) {
                        Platform.runLater(() -> {
                            cargarPantalla(contenido, "/com/example/alexandriafrontend/Lector.fxml",
                                    (LectorController controller) -> {
                                        controller.setIdLibro(libro.getId());
                                        controller.cargarLibroDesdeURL(urlFirmada);
                                    });
                        });
                    }
                });
            }
        });
    }

}
