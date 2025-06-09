package com.example.alexandriafrontend.model;

import java.util.List;

public class Anotacion {
    private int start;
    private int end;
    private List<String> estilos;
    private String comentario;

    public Anotacion() {}

    public Anotacion(int start, int end, List<String> estilos, String comentario) {
        this.start = start;
        this.end = end;
        this.estilos = estilos;
        this.comentario = comentario;
    }

    // Getters y setters
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }

    public int getEnd() { return end; }
    public void setEnd(int end) { this.end = end; }

    public List<String> getEstilos() { return estilos; }
    public void setEstilos(List<String> estilos) { this.estilos = estilos; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}

