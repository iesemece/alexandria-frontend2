package com.example.alexandriafrontend.model;

public class Usuario {
    private String nombre;
    private String apellido;

    public Usuario() {
        // Constructor vacío necesario para Gson
    }

    public Usuario( String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
}
