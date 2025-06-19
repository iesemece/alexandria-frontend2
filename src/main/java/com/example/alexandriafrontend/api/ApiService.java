package com.example.alexandriafrontend.api;

import com.example.alexandriafrontend.model.Anotacion;
import com.example.alexandriafrontend.model.Libro;
import com.example.alexandriafrontend.model.UsuarioListado;
import com.example.alexandriafrontend.request.AnotacionesRequest;
import com.example.alexandriafrontend.response.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface ApiService {

    /**
     * Inicia sesión con el email y la contraseña proporcionados.
     *
     * @param email Email del usuario.
     * @param contrasena Contraseña del usuario.
     * @return Llamada que devuelve la respuesta de login con token y datos del usuario.
     */
    @GET("/api/usuarios/login")
    Call<LoginResponse> login(
            @Query("email") String email,
            @Query("contrasena") String contrasena
    );

    /**
     * Registra un nuevo usuario en la aplicación.
     *
     * @param primerNombre Primer nombre del usuario.
     * @param segundoNombre Segundo nombre del usuario.
     * @param email Email del usuario.
     * @param contrasena Contraseña del usuario.
     * @param role Rol del usuario (por ejemplo, "USER" o "ADMIN").
     * @return Llamada que indica si el registro fue exitoso.
     */
    @FormUrlEncoded
    @POST("/api/usuarios/registrar")
    Call<ResponseBody> registrar(
            @Field("primerNombre") String primerNombre,
            @Field("segundoNombre") String segundoNombre,
            @Field("email") String email,
            @Field("contrasena") String contrasena,
            @Field("role") String role
    );

    /**
     * Obtiene la lista completa de libros disponibles.
     *
     * @return Llamada que devuelve una lista de libros.
     */
    @GET("api/libros/todos")
    Call<List<Libro>> obtenerTodosLibros();

    /**
     * Busca libros que contengan el texto proporcionado.
     *
     * @param texto Texto de búsqueda.
     * @return Llamada que devuelve los libros que coinciden con la búsqueda.
     */
    @GET("/api/libros/buscar")
    Call<List<Libro>> buscarLibros(@Query("texto") String texto);

    /**
     * Obtiene la lista de libros marcados como favoritos por el usuario autenticado.
     *
     * @param token Token de autorización JWT.
     * @return Llamada que devuelve una lista de libros favoritos.
     */
    @GET("/api/biblioteca/favoritos")
    Call<List<Libro>> buscarLibrosFavoritos(@Header("Authorization") String token);

    /**
     * Obtiene la lista de libros que el usuario tiene en lectura actualmente.
     *
     * @param token Token de autorización JWT.
     * @return Llamada que devuelve una lista de libros en lectura.
     */
    @GET("/api/biblioteca/enlecturas")
    Call<List<Libro>> buscarLibrosLecturas(@Header("Authorization") String token);

    /**
     * Obtiene el archivo del libro en base a su ID.
     *
     * @param id ID del libro.
     * @return Llamada que devuelve el contenido del archivo como respuesta binaria.
     */
    @GET("/api/libros/archivo-url")
    Call<ResponseBody> obtenerArchivoUrl(@Query("id") Long id);

    /**
     * Solicita una URL firmada para acceder a un archivo EPUB.
     *
     * @param nombreArchivo Nombre del archivo EPUB.
     * @return Llamada que devuelve el archivo firmado.
     */
    @GET("api/epubs/{nombreArchivo}")
    Call<ResponseBody> obtenerUrlFirmada(@Path("nombreArchivo") String nombreArchivo);

    /**
     * Registra un libro como "en lectura" para el usuario.
     *
     * @param token Token de autorización.
     * @param libroId ID del libro.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @POST("/api/biblioteca/guardar/enlectura")
    Call<Void> registrarLectura(@Header("Authorization") String token, @Query("libroId") Long libroId);

    /**
     * Elimina un libro del listado de "en lectura".
     *
     * @param token Token de autorización.
     * @param libroId ID del libro.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @PUT("/api/biblioteca/eliminar/enlectura")
    Call<Void> eliminarLectura(@Header("Authorization") String token, @Query("libroId") Long libroId);

    /**
     * Añade un libro a la lista de favoritos del usuario.
     *
     * @param token Token de autorización.
     * @param libroId ID del libro.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @POST("/api/biblioteca/guardar/favoritos")
    Call<Void> registrarFavoritos(@Header("Authorization") String token, @Query("libroId") Long libroId);

    /**
     * Elimina un libro de la lista de favoritos del usuario.
     *
     * @param token Token de autorización.
     * @param libroId ID del libro.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @PUT("/api/biblioteca/eliminar/favoritos")
    Call<Void> eliminarFavoritos(@Header("Authorization") String token, @Query("libroId") Long libroId);

    /**
     * Guarda las anotaciones realizadas por el usuario en un libro.
     *
     * @param token Token de autorización.
     * @param request Objeto que contiene las anotaciones a guardar.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @POST("/api/biblioteca/guardar/anotaciones")
    Call<Void> guardarAnotaciones(@Header("Authorization") String token, @Body AnotacionesRequest request);

    /**
     * Recupera las anotaciones realizadas por el usuario en un libro.
     *
     * @param token Token de autorización.
     * @param libroId ID del libro.
     * @return Mapa de página a lista de anotaciones.
     */
    @GET("/api/biblioteca/recuperar/anotaciones")
    Call<Map<Integer, List<Anotacion>>> obtenerAnotaciones(@Header("Authorization") String token, @Query("libroId") Long libroId);

    /**
     * Recupera las distintas categorias de los libros.
     *
     * @return Lista de String de las categorias.
     */

    @GET("/api/libros/categorias")
    Call<List<String>> obtenerCategorias();

    /**
     * Filtra todos los libros por categorias.
     *
     * @param categoria String de la categoria.
     * @return Llamada que devuelve una lista de libros filtrada según la categoria.
     */

    @GET("/api/libros/categorias/libros")
    Call<List<Libro>> obtenerLibrosPorCategoria(@Query("categoria") String categoria);

    /**
     * Obtiene la lista de usuarios registrados.
     *
     * @param token Token de autorización.
     * @return Lista de usuarios en formato reducido.
     */
    @GET("/api/usuarios/lista")
    Call<List<UsuarioListado>> obtenerUsuarios(@Header("Authorization") String token);

    /**
     * Obtiene los libros compartidos entre dos usuarios.
     *
     * @param usuarioId ID del usuario actual.
     * @param usuarioDestinoId ID del usuario destino.
     * @param token Token de autorización.
     * @return Lista de libros compartidos.
     */
    @GET("/lecturas-compartidas/entre")
    Call<List<Libro>> obtenerLibrosCompartidos(
            @Query("usuarioId") Long usuarioId,
            @Query("usuarioDestinoId") Long usuarioDestinoId,
            @Header("Authorization") String token
    );

    /**
     * Comparte un libro con otro usuario.
     *
     * @param usuarioId ID del usuario actual.
     * @param usuarioDestinoId ID del destinatario.
     * @param libroId ID del libro.
     * @param token Token de autorización.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @POST("/lecturas-compartidas/compartir")
    Call<Void> compartirLibro(
            @Query("usuarioId") Long usuarioId,
            @Query("usuarioDestinoId") Long usuarioDestinoId,
            @Query("libroId") Long libroId,
            @Header("Authorization") String token
    );

    /**
     * Obtiene el ID de la lectura compartida entre dos usuarios y un libro.
     *
     * @param usuarioId1 ID del primer usuario.
     * @param usuarioId2 ID del segundo usuario.
     * @param libroId ID del libro.
     * @param token Token de autorización.
     * @return ID de la lectura compartida.
     */
    @GET("/lecturas-compartidas/lecturas-compartidas/id")
    Call<Long> obtenerLecturaCompartidaId(
            @Query("usuarioId1") Long usuarioId1,
            @Query("usuarioId2") Long usuarioId2,
            @Query("libroId") Long libroId,
            @Header("Authorization") String token
    );

    /**
     * Guarda anotaciones colaborativas en una lectura compartida.
     *
     * @param lecturaCompartidaId ID de la lectura compartida.
     * @param request Anotaciones a guardar.
     * @return Llamada vacía indicando éxito o fallo.
     */
    @POST("/lecturas-compartidas/lecturas-compartidas/{lecturaCompartidaId}/anotaciones")
    Call<Void> guardarAnotacionesCompartidas(
            @Path("lecturaCompartidaId") Long lecturaCompartidaId,
            @Body AnotacionesRequest request
    );

    /**
     * Obtiene las anotaciones colaborativas de una lectura compartida.
     *
     * @param lecturaCompartidaId ID de la lectura compartida.
     * @return Mapa de página a lista de anotaciones compartidas.
     */
    @GET("/lecturas-compartidas/lecturas-compartidas/{lecturaCompartidaId}/anotaciones")
    Call<Map<Integer, List<Anotacion>>> obtenerAnotacionesCompartidas(
            @Path("lecturaCompartidaId") Long lecturaCompartidaId
    );



}
