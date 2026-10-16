package com.gem.controller;

import com.gem.model.Medicamento;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.RemoverMedicamentoService;
import com.gem.view.ViewManager;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.springframework.stereotype.Controller;

@Controller
public class RemoverMedicamentoController {

    private final RemoverMedicamentoService removerMedicamentoService;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    public RemoverMedicamentoController(
            RemoverMedicamentoService removerMedicamentoService,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager) {

        this.removerMedicamentoService = removerMedicamentoService;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label acessoLabel;

    @FXML
    private ComboBox<Medicamento> campoMedicamento;

    @FXML
    private ComboBox<Integer> campoQuantidade;

    @FXML
    private TextField campoMotivo;

    @FXML
    public void initialize() {
        configurarExibicaoMedicamentos();
        configurarSelecaoMedicamento();
        carregarUsuario();
        carregarMedicamentos();
    }

    private void configurarExibicaoMedicamentos() {
        if (campoMedicamento == null) return;

        campoMedicamento.setConverter(new StringConverter<>() {

            @Override
            public String toString(Medicamento medicamento) {
                if (medicamento == null) {
                    return "";
                }

                return medicamento.getPrincipioAtivo()
                        + " - " + medicamento.getDose()
                        + " (" + medicamento.getTipoMed() + ")"
                        + (medicamento.getLaboratorio() != null ? " - " + medicamento.getLaboratorio() : "");
            }

            @Override
            public Medicamento fromString(String texto) {
                return null;
            }
        });
    }

    private void configurarSelecaoMedicamento() {
        if (campoMedicamento == null) return;

        campoMedicamento.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, anterior, selecionado) -> {
                    atualizarQuantidades();
                });
    }

    private void carregarUsuario() {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        if (usuario == null) {
            if (usuarioLabel != null) usuarioLabel.setText("Usuário não identificado");
            if (acessoLabel != null) acessoLabel.setText("Acesso não identificado");
            return;
        }

        String cargo = String.valueOf(usuario.getCargo());

        if (usuarioLabel != null) usuarioLabel.setText(cargo);
        if (acessoLabel != null) acessoLabel.setText("Acesso: " + cargo);
    }

    private void carregarMedicamentos() {
        if (campoMedicamento == null) return;

        try {
            List<Medicamento> medicamentos =
                    removerMedicamentoService.listarMedicamentosDisponiveis();

            campoMedicamento.getItems().setAll(medicamentos);

        } catch (RuntimeException e) {
            exibirAlerta(
                    AlertType.ERROR,
                    "Erro ao carregar medicamentos",
                    "Não foi possível carregar os medicamentos disponíveis."
            );
        }
    }

    private void atualizarQuantidades() {
        if (campoQuantidade == null || campoMedicamento == null) return;

        campoQuantidade.getItems().clear();

        Medicamento selecionado =
                campoMedicamento.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            return;
        }

        try {
            int quantidadeDisponivel =
                    removerMedicamentoService.obterQuantidadeDisponivel(selecionado);

            if (quantidadeDisponivel <= 0) {
                campoQuantidade.getItems().add(0);
                campoQuantidade.getSelectionModel().selectFirst();
            } else {
                for (int quantidade = 1;
                        quantidade <= quantidadeDisponivel;
                        quantidade++) {

                    campoQuantidade.getItems().add(quantidade);
                }
                campoQuantidade.getSelectionModel().selectLast();
            }

        } catch (RuntimeException e) {
            exibirAlerta(
                    AlertType.ERROR,
                    "Erro ao consultar estoque",
                    "Não foi possível consultar a quantidade disponível."
            );
        }
    }

    @FXML
    public void remover() {
        if (campoMedicamento == null) return;

        Medicamento medicamento =
                campoMedicamento.getSelectionModel().getSelectedItem();

        String motivo = campoMotivo != null ? campoMotivo.getText() : "";

        if (medicamento == null) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Medicamento não selecionado",
                    "Selecione o medicamento que deseja remover do catálogo."
            );
            return;
        }

        // 1. Confirmação da remoção (Critério US-09)
        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Confirma a remoção do medicamento do catálogo?");
        confirmacao.setContentText(
                "Medicamento: " + medicamento.getPrincipioAtivo() + " (" + medicamento.getDose() + ")"
                + (motivo != null && !motivo.isBlank() ? "\nMotivo: " + motivo.trim() : "")
        );

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        // 2. Remoção do catálogo e exibição de mensagem de confirmação
        try {
            removerMedicamentoService.removerDoCatalogo(
                    medicamento.getId(),
                    motivo != null ? motivo.trim() : ""
            );

            exibirAlerta(
                    AlertType.INFORMATION,
                    "Remoção realizada",
                    "Medicamento removido com sucesso!"
            );

            campoMedicamento.getSelectionModel().clearSelection();
            if (campoQuantidade != null) campoQuantidade.getItems().clear();
            if (campoMotivo != null) campoMotivo.clear();

            carregarMedicamentos();

        } catch (RuntimeException e) {
            exibirAlerta(
                    AlertType.ERROR,
                    "Erro na remoção",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Não foi possível concluir a remoção do medicamento."
            );
        }
    }

    @FXML
    public void voltar() {
        try {
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            viewManager.mostrarEstoque(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirEstoque() {
        voltar();
    }

    @FXML
    public void abrirMovimentacao() {
        try {
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            Usuario usuario = sessaoUsuario.getUsuarioAtual();
            if (usuario != null && usuario.getCargo() != null) {
                switch (usuario.getCargo()) {
                    case COORDENADOR -> viewManager.mostrarCoordenador(stage);
                    case FUNCIONARIO_CAF -> viewManager.mostrarMovimentacao(stage);
                    case FUNCIONARIO_SAT -> viewManager.mostrarTecnico(stage);
                }
            } else {
                viewManager.mostrarCoordenador(stage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirHistorico() {
        try {
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            viewManager.mostrarHistorico(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirCadastroMedicamento() {
        try {
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            viewManager.mostrarCadastroMedicamento(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sair() {
        try {
            sessaoUsuario.encerrar();
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            viewManager.mostrarLogin(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exibirAlerta(
            AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
