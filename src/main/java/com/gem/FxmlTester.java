package com.gem;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class FxmlTester implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("test-fxml")) {
            Platform.startup(() -> {
                try {
                    URL url = getClass().getResource("/view/TelaHistorico.fxml");
                    System.out.println("URL: " + url);
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    System.out.println("FXML carregado com sucesso!");
                } catch (Exception e) {
                    System.out.println("ERRO AO CARREGAR FXML:");
                    e.printStackTrace();
                } finally {
                    Platform.exit();
                    System.exit(0);
                }
            });
        }
    }
}
