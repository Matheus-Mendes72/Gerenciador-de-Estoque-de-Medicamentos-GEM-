package com.gem.view;

import com.gem.controller.MovimentacaoController;
import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.springframework.stereotype.Component;

@Component
public class MovimentacaoViewController {

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label acessoLabel;

    @FXML
    private Label resumoDispensadorLabel;

    @FXML
    private Label resumoDataHoraLabel;

    @FXML
    private Label resumoDestinoLabel;

    @FXML
    private Label resumoItemLabel;

    @FXML
    private TextField setorField;

    @FXML
    private TextField medicamentoField;

    @FXML
    private TextField quantidadeField;

    @FXML
    private TextField motivoField;

    @FXML
    private ComboBox<String> setorComboBox;

    private final MovimentacaoController movimentacaoController;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    public MovimentacaoViewController(
            MovimentacaoController movimentacaoController,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager
    ) {
        this.movimentacaoController = movimentacaoController;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private void initialize() {

        if (!sessaoUsuario.estaAutenticado()) {
            return;
        }

        Usuario usuario =
                sessaoUsuario.getUsuarioAtual();

        usuarioLabel.setText(
                usuario.getCargo().name()
        );

        acessoLabel.setText(
                "Acesso: " + formatarCargo(usuario)
        );

        resumoDispensadorLabel.setText(
                formatarCargo(usuario)
        );

        if (setorComboBox != null) {

            setorComboBox.getItems().clear();

            for (SetorHosp setor : SetorHosp.values()) {
                setorComboBox.getItems().add(
                        formatarSetor(setor)
                );
            }
        }
    }

    @FXML
    private void confirmarSaida() {

        Usuario usuario =
                sessaoUsuario.getUsuarioAtual();

        if (usuario == null) {

            mostrarErro(
                    "Usuário não autenticado."
            );

            return;
        }

        Integer estoqueId =
                obterEstoqueId();

        Integer quantidade =
                obterQuantidade();

        SetorHosp setor =
                obterSetor();

        if (estoqueId == null
                || quantidade == null
                || setor == null) {

            return;
        }

        String motivo =
                motivoField != null
                        ? motivoField.getText()
                        : "";

        try {

            Historico historico =
                    movimentacaoController.registrarSaida(
                            estoqueId,
                            quantidade,
                            setor,
                            motivo,
                            usuario
                    );

            atualizarResumo(historico);

            mostrarSucesso(
                    "Saída registrada com sucesso."
            );

            limparCampos();

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Não foi possível registrar a saída."
            );
        }
    }

    @FXML
    private void confirmarRetorno() {

        Usuario usuario =
                sessaoUsuario.getUsuarioAtual();

        if (usuario == null) {

            mostrarErro(
                    "Usuário não autenticado."
            );

            return;
        }

        Integer estoqueId =
                obterEstoqueId();

        Integer quantidade =
                obterQuantidade();

        SetorHosp setor =
                obterSetor();

        if (estoqueId == null
                || quantidade == null
                || setor == null) {

            return;
        }

        String motivo =
                motivoField != null
                        ? motivoField.getText()
                        : "";

        try {

            Historico historico =
                    movimentacaoController.registrarRetorno(
                            estoqueId,
                            quantidade,
                            setor,
                            motivo,
                            usuario
                    );

            atualizarResumo(historico);

            mostrarSucesso(
                    "Retorno registrado com sucesso."
            );

            limparCampos();

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Não foi possível registrar o retorno."
            );
        }
    }

    @FXML
    private void sair() {

        sessaoUsuario.encerrar();

        try {

            Stage stage =
                    (Stage) usuarioLabel
                            .getScene()
                            .getWindow();

            viewManager.mostrarLogin(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível voltar para o login."
            );
        }
    }

    private Integer obterEstoqueId() {

        String texto =
                medicamentoField.getText();

        if (texto == null || texto.isBlank()) {

            mostrarErro(
                    "Informe o ID do medicamento."
            );

            return null;
        }

        try {

            return Integer.valueOf(
                    texto.trim()
            );

        } catch (NumberFormatException e) {

            mostrarErro(
                    "O ID do medicamento deve ser numérico."
            );

            return null;
        }
    }

    private Integer obterQuantidade() {

        String texto =
                quantidadeField.getText();

        if (texto == null || texto.isBlank()) {

            mostrarErro(
                    "Informe a quantidade."
            );

            return null;
        }

        try {

            int quantidade =
                    Integer.parseInt(
                            texto.trim()
                    );

            if (quantidade <= 0) {

                mostrarErro(
                        "A quantidade deve ser maior que zero."
                );

                return null;
            }

            return quantidade;

        } catch (NumberFormatException e) {

            mostrarErro(
                    "A quantidade deve ser um número inteiro."
            );

            return null;
        }
    }

    private SetorHosp obterSetor() {

        String texto =
                setorComboBox.getValue();

        if (texto == null || texto.isBlank()) {

            mostrarErro(
                    "Selecione o setor."
            );

            return null;
        }

        for (SetorHosp setor : SetorHosp.values()) {

            if (formatarSetor(setor).equals(texto)) {
                return setor;
            }
        }

        mostrarErro("Setor inválido.");

        return null;
    }

    private void atualizarResumo(
            Historico historico
    ) {

        if (resumoDataHoraLabel != null) {

            resumoDataHoraLabel.setText(
                    historico.getDataMov().toString()
            );
        }

        if (resumoDestinoLabel != null) {

            resumoDestinoLabel.setText(
                    formatarSetor(
                            historico.getSetorHosp()
                    )
            );
        }

        if (resumoItemLabel != null) {

            resumoItemLabel.setText(
                    "Medicamento ID: "
                            + historico
                                    .getEstoque()
                                    .getId()
                            + " | Quantidade: "
                            + historico.getQuantidade()
            );
        }
    }

    private void limparCampos() {

        medicamentoField.clear();
        quantidadeField.clear();

        if (motivoField != null) {
            motivoField.clear();
        }
    }

    private String formatarCargo(
            Usuario usuario
    ) {

        return switch (usuario.getCargo()) {

            case COORDENADOR ->
                    "Coordenador";

            case FUNCIONARIO_CAF ->
                    "Farmacêutico";

            case FUNCIONARIO_SAT ->
                    "Técnico de Enfermagem";
        };
    }

    private String formatarSetor(
            SetorHosp setor
    ) {

        return switch (setor) {

            case SATELITE ->
                    "Satélite";

            case EMERGENCIA ->
                    "Emergência";

            case CLINICA_MEDICA ->
                    "Clínica Médica";

            case PEDIATRIA ->
                    "Pediatria";

            case MATERNIDADE ->
                    "Maternidade";

            case ESTABILIZACAO ->
                    "Estabilização";
        };
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

    private void mostrarSucesso(
            String mensagem
    ) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("GEM");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}