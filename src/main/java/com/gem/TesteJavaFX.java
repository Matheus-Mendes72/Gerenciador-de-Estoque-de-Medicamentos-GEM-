package com.gem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TesteJavaFX extends Application {

    @Override
    public void start(Stage stage) {
        Label texto = new Label("JavaFX está funcionando!");

        Scene cena = new Scene(texto, 400, 200);

        stage.setTitle("Teste JavaFX");
        stage.setScene(cena);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}