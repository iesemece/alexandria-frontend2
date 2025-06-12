package com.example.alexandriafrontend.api;

import com.example.alexandriafrontend.model.Anotacion;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.request.AnotacionesRequest;
import com.example.alexandriafrontend.response.LibroResponse;
import com.example.alexandriafrontend.response.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {

    @GET("/api/usuarios/login")
    Call<LoginResponse> login(
            @Query("email") String email,
            @Query("contrasena") String contrasena
    );

    @FormUrlEncoded
    @POST("/api/usuarios/registrar")
    Call<ResponseBody> registrar(
            @Field("primerNombre") String primerNombre,
            @Field("segundoNombre") String segundoNombre,
            @Field("email") String email,
            @Field("contrasena") String contrasena,
            @Field("role") String role
    );

    @GET("api/libros/todos")
    Call<List<LibroResponse>> obtenerTodosLibros();

    @GET("/api/libros/buscar")
    Call<List<LibroResponse>> buscarLibros(@Query("texto") String texto);

    @GET("/api/biblioteca/favoritos")
    Call<List<LibroResponse>> buscarLibrosFavoritos(@Header("Authorization") String token);

    @GET("/api/biblioteca/lecturas")
    Call<List<LibroResponse>> buscarLibrosLecturas(@Header("Authorization") String token);

    @GET("/api/libros/archivo-url")
    Call<ResponseBody> obtenerArchivoUrl(@Query("id") Long id);

    @GET("api/epubs/{nombreArchivo}")
    Call<ResponseBody> obtenerUrlFirmada(@Path("nombreArchivo") String nombreArchivo);

    @POST("/api/biblioteca/enlectura")
    Call<Void> registrarLectura(@Header("Authorization") String token, @Query("libroId") Long libroId);

    @POST("/api/biblioteca/guardar-anotaciones")
    Call<Void> guardarAnotaciones(@Header("Authorization") String token, @Body AnotacionesRequest request);

    @GET("/api/biblioteca/recuperar-anotaciones")
    Call<Map<Integer, List<Anotacion>>> obtenerAnotaciones(@Header("Authorization") String token, @Query("libroId") Long libroId);

    @GET("/api/usuarios/lista")
    Call<List<UsuarioListado>> obtenerUsuarios(@Header("Authorization") String token);

    @GET("/lecturas-compartidas/entre")
    Call<List<LibroResponse>> obtenerLibrosCompartidos(
            @Query("usuarioId") Long usuarioId,
            @Query("usuarioDestinoId") Long usuarioDestinoId,
            @Header("Authorization") String token
    );

    @POST("/lecturas-compartidas/compartir")
    Call<Void> compartirLibro(
            @Query("usuarioId") Long usuarioId,
            @Query("usuarioDestinoId") Long usuarioDestinoId,
            @Query("libroId") Long libroId,
            @Header("Authorization") String token
    );

    @GET("/lecturas-compartidas/lecturas-compartidas/id")
    Call<Long> obtenerLecturaCompartidaId(
            @Query("usuarioId1") Long usuarioId1,
            @Query("usuarioId2") Long usuarioId2,
            @Query("libroId") Long libroId,
            @Header("Authorization") String token
    );

    // Guardar anotaciones colaborativas
    @POST("/lecturas-compartidas/lecturas-compartidas/{lecturaCompartidaId}/anotaciones")
    Call<Void> guardarAnotacionesCompartidas(
            @Path("lecturaCompartidaId") Long lecturaCompartidaId,
            @Body AnotacionesRequest request
    );

    // Obtener anotaciones colaborativas
    @GET("/lecturas-compartidas/lecturas-compartidas/{lecturaCompartidaId}/anotaciones")
    Call<Map<Integer, List<Anotacion>>> obtenerAnotacionesCompartidas(
            @Path("lecturaCompartidaId") Long lecturaCompartidaId
    );




}
