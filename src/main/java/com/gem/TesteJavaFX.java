package com.gem;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class TesteJavaFX {

    public void mostrar(Stage stage) {
        Label texto = new Label("JavaFX está funcionando!");

        Scene cena = new Scene(texto, 400, 200);

        stage.setTitle("Gerenciador de Estoque de Medicamentos");
        stage.setScene(cena);
        stage.show();
    }
}