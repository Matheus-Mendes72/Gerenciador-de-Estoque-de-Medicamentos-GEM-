package com.gem.view;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.AutenticacaoService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML
    private ComboBox<String> cargoComboBox;

    @FXML
    private PasswordField senhaField;

    private final AutenticacaoService autenticacaoService;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    public LoginController(
            AutenticacaoService autenticacaoService,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager
    ) {
        this.autenticacaoService = autenticacaoService;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private void login() {

        String cargoSelecionado = cargoComboBox.getValue();
        String senha = senhaField.getText();

        /*
         * US-01
         * O cargo é obrigatório.
         */
        if (cargoSelecionado == null
                || cargoSelecionado.isBlank()) {

            mostrarErro(
                    "Selecione um cargo para continuar."
            );

            return;
        }

        /*
         * US-01
         * A senha é obrigatória.
         */
        if (senha == null
                || senha.isBlank()) {

            mostrarErro(
                    "Informe a senha para continuar."
            );

            return;
        }

        CargoProfissional cargo;

        try {

            cargo = converterCargo(cargoSelecionado);

        } catch (IllegalArgumentException e) {

            mostrarErro("Cargo inválido.");

            return;
        }

        try {

            /*
             * US-02
             * O backend valida a senha correspondente
             * ao cargo selecionado.
             */
            Usuario usuario =
                    autenticacaoService.autenticar(
                            cargo,
                            senha
                    );

            /*
             * Se chegou aqui, a autenticação foi
             * realizada com sucesso.
             */
            sessaoUsuario.iniciar(usuario);

            abrirTelaPrincipal(usuario);

        } catch (BadCredentialsException e) {

            mostrarErro(
                    "Senha incorreta para o cargo selecionado."
            );

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível realizar o acesso ao sistema."
            );
        }
    }

    private CargoProfissional converterCargo(
            String cargoSelecionado
    ) {

        return switch (cargoSelecionado) {

            case "Coordenador" ->
                    CargoProfissional.COORDENADOR;

            case "Farmacêutico" ->
                    CargoProfissional.FUNCIONARIO_CAF;

            case "Técnico de Enfermagem" ->
                    CargoProfissional.FUNCIONARIO_SAT;

            default ->
                    throw new IllegalArgumentException(
                            "Cargo inválido."
                    );
        };
    }

    private void abrirTelaPrincipal(
            Usuario usuario
    ) {

        try {

            Stage stage =
                    (Stage) senhaField
                            .getScene()
                            .getWindow();

            switch (usuario.getCargo()) {

                case COORDENADOR ->
                        viewManager.mostrarCoordenador(stage);

                case FUNCIONARIO_CAF ->
                        viewManager.mostrarMovimentacao(stage);

                case FUNCIONARIO_SAT ->
                        viewManager.mostrarTecnico(stage);
            }

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Erro ao abrir a tela principal."
            );
        }
    }

    private void mostrarErro(
            String mensagem
    ) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("GEM - Acesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}