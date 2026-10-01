package com.gem.view;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.springframework.stereotype.Component;

@Component
public class CoordenadorViewController {

    @FXML
    private BorderPane root;

    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    public CoordenadorViewController(
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager
    ) {
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private void initialize() {

        validarCoordenador();
    }

    @FXML
    private void abrirMovimentacao() {

        if (!validarCoordenadorSilenciosamente()) {
            return;
        }

        try {

            Stage stage =
                    (Stage) root.getScene().getWindow();

            viewManager.mostrarMovimentacao(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível abrir a movimentação."
            );
        }
    }

    @FXML
    private void sair() {

        sessaoUsuario.encerrar();

        try {

            Stage stage =
                    (Stage) root.getScene().getWindow();

            viewManager.mostrarLogin(stage);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void validarCoordenador() {

        Usuario usuario =
                sessaoUsuario.getUsuarioAtual();

        if (usuario == null
                || usuario.getCargo()
                        != CargoProfissional.COORDENADOR) {

            throw new SecurityException(
                    "Acesso permitido somente ao coordenador."
            );
        }
    }

    private boolean validarCoordenadorSilenciosamente() {

        try {

            validarCoordenador();

            return true;

        } catch (SecurityException e) {

            mostrarErro(e.getMessage());

            return false;
        }
    }

    private void mostrarErro(
            String mensagem
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("GEM");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}