package com.gem.view;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class ViewManager {

    private final ApplicationContext springContext;

    public ViewManager(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void mostrarLogin(Stage stage) throws IOException {

        FXMLLoader loader = criarLoader("/view/TelaLogin.fxml");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("GEM - Acesso ao Sistema");
        stage.setResizable(false);
        stage.show();
    }

    public void mostrarMovimentacao(Stage stage) throws IOException {

        FXMLLoader loader =
                criarLoader("/view/TelaMovimentacao.fxml");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("GEM - Movimentação de Medicamentos");
        stage.setResizable(false);
        stage.show();
    }

    public void mostrarCoordenador(Stage stage) throws IOException {

        FXMLLoader loader =
                criarLoader("/view/TelaCoordenador.fxml");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("GEM - Área do Coordenador");
        stage.setResizable(false);
        stage.show();
    }

    public void mostrarTecnico(Stage stage) throws IOException {

        FXMLLoader loader =
                criarLoader("/view/TelaTecnico.fxml");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("GEM - Área do Técnico de Enfermagem");
        stage.setResizable(false);
        stage.show();
    }

    private FXMLLoader criarLoader(String caminho) {

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource(caminho));

        loader.setControllerFactory(
                springContext::getBean
        );

        return loader;
    }
}