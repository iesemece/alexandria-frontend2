package com.example.alexandriafrontend.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class LecturasCompartidasController {

    @FXML
    private ListView<String> listaRecomendaciones;

    @FXML
    public void initialize() {
        ObservableList<String> librosCompartidos = FXCollections.observableArrayList(
                "El Quijote",
                "Cien años de soledad",
                "La casa de Bernarda Alba",
                "Rayuela",
                "El amor en los tiempos del cólera"
        );

        listaRecomendaciones.setItems(librosCompartidos);
    }
}
