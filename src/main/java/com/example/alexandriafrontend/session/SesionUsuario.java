package com.example.alexandriafrontend.session;

import com.example.alexandriafrontend.model.Usuario;
import com.example.alexandriafrontend.utils.JwtUtils;
import com.google.gson.JsonObject;

public class SesionUsuario {

    private static SesionUsuario instancia;
    private Usuario usuarioActual;
    private String token;

    private SesionUsuario() {}

    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void iniciarSesionConToken(String token) {
        this.token = token;
        JsonObject payload = JwtUtils.decodificarToken(token);

        String nombre = payload.has("primerNombre") ? payload.get("primerNombre").getAsString() : "";
        String apellido = payload.has("segundoNombre") ? payload.get("segundoNombre").getAsString() : "";

        this.usuarioActual = new Usuario(nombre, apellido);
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public String getToken() {
        return token;
    }

    public boolean hayUsuarioLogueado() {
        return usuarioActual != null;
    }

    public void cerrarSesion() {
        usuarioActual = null;
        token = null;
    }

}


