package com.example.alexandriafrontend.response;


public class LoginResponse {
    private String token;
    private String primerNombre;
    private String segundoNombre;

    public LoginResponse(String token, String primerNombre, String segundoNombre) {
        this.token = token;
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }
}
