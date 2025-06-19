package com.example.alexandriafrontend.utils;

import com.example.alexandriafrontend.model.Libro;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.io.InputStream;

public class ItemLibro extends StackPane {

    @FXML private ImageView coverImage;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;

    private Libro libro;

    public ItemLibro(Libro libro) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/alexandriafrontend/ItemLibro.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
            getStylesheets().add(getClass().getResource("/styles/ItemLibro.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (libro != null) {
            updateItem(libro);
        }
    }

    public void updateItem(Libro libro) {
        if (libro == null) return;

        this.libro = libro;
        titleLabel.setText(libro.getTitulo());
        authorLabel.setText(libro.getAutor());

        coverImage.setImage(loadImage(libro));
    }

    private Image loadImage(Libro libro) {
        String imageName = determineImageName(libro);
        String path = "/image/" + imageName;

        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) return new Image(is);
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
        }

        return loadDefaultImage();
    }

    private Image loadDefaultImage() {
        InputStream is = getClass().getResourceAsStream("/image/default.png");
        if (is != null) return new Image(is);
        return new Image("https://via.placeholder.com/140x190?text=No+Image");
    }

    private String determineImageName(Libro libro) {
        if (libro.getCategoria() == null || libro.getCategoria().isEmpty()) {
            return "default.png";
        }

        return mapCategoriaToImage(libro.getCategoria());
    }

    private String mapCategoriaToImage(String categoria) {
        switch (categoria) {
            case "Romance": return "romance.png";
            case "Terror": return "terror.png";
            case "Fantasia": return "fantasia.png";
            case "Suspense": return "suspense.png";
            case "Ciencia Ficcion": return "ciencia_ficcion.png";
            default: return "default.png";
        }
    }
    public Libro getLibro() {
        return libro;
    }
}
