package com.example.alexandriafrontend.response;

public class LibroResponse {
    private Long id;
    private String titulo;
    private String autor;

    public LibroResponse(Long id, String titulo, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }
}

