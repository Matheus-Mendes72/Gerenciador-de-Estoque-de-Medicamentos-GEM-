package com.gem;

import com.gem.view.ViewManager;

import javafx.application.Application;
import javafx.stage.Stage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {

        springContext =
                SpringApplication.run(
                        DemoApplication.class
                );
    }

    @Override
    public void start(Stage stage) {

        ViewManager viewManager =
                springContext.getBean(
                        ViewManager.class
                );

        try {

            viewManager.mostrarLogin(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}