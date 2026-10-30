package com.gem.view;

import com.gem.controller.MovimentacaoController;
import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.security.SessaoUsuario;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.springframework.stereotype.Component;

import java.util.List;

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
    private ComboBox<Estoque> medicamentoComboBox;

    @FXML
    private TextField quantidadeField;

    @FXML
    private TextField motivoField;

    @FXML
    private ComboBox<String> setorComboBox;

    private final MovimentacaoController movimentacaoController;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;
    private final EstoqueRepository estoqueRepository;

    public MovimentacaoViewController(
            MovimentacaoController movimentacaoController,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager,
            EstoqueRepository estoqueRepository
    ) {
        this.movimentacaoController = movimentacaoController;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
        this.estoqueRepository = estoqueRepository;
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

        carregarComboMedicamentos();
    }

    private void carregarComboMedicamentos() {
        if (medicamentoComboBox == null || estoqueRepository == null) {
            return;
        }

        medicamentoComboBox.getItems().clear();

        List<Estoque> lista = estoqueRepository.findAllAtivos();
        medicamentoComboBox.setItems(FXCollections.observableArrayList(lista));

        medicamentoComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Estoque estoque) {
                if (estoque == null) {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                if (estoque.getPrincipioAtivo() != null) {
                    sb.append(estoque.getPrincipioAtivo());
                } else {
                    sb.append("Item #").append(estoque.getId());
                }

                if (estoque.getDose() != null && !estoque.getDose().isBlank()) {
                    sb.append(" ").append(estoque.getDose());
                }

                if (estoque.getLote() != null && !estoque.getLote().isBlank()) {
                    sb.append(" | Lote: ").append(estoque.getLote());
                }

                if (estoque.getQuantidade() != null) {
                    sb.append(" (Saldo: ").append(estoque.getQuantidade()).append(")");
                }

                return sb.toString();
            }

            @Override
            public Estoque fromString(String string) {
                return null;
            }
        });
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
            carregarComboMedicamentos();

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
            carregarComboMedicamentos();

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

    @FXML
    private void abrirHistorico() {

        try {

            Stage stage =
                    (Stage) usuarioLabel
                            .getScene()
                            .getWindow();

            viewManager.mostrarHistorico(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível abrir o histórico."
            );
        }
    }

    @FXML
    private void abrirEstoque() {

        try {

            Stage stage =
                    (Stage) usuarioLabel
                            .getScene()
                            .getWindow();

            viewManager.mostrarEstoque(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível abrir o estoque."
            );
        }
    }

    @FXML
    private void abrirCadastroMedicamento() {

        try {

            Stage stage =
                    (Stage) usuarioLabel
                            .getScene()
                            .getWindow();

            viewManager.mostrarCadastroMedicamento(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível abrir o cadastro de medicamentos."
            );
        }
    }

    @FXML
    private void abrirRemoverMedicamento() {

        try {

            Stage stage =
                    (Stage) usuarioLabel
                            .getScene()
                            .getWindow();

            viewManager.mostrarRemoverMedicamento(stage);

        } catch (Exception e) {

            e.printStackTrace();

            mostrarErro(
                    "Não foi possível abrir a remoção de medicamentos."
            );
        }
    }

    private Integer obterEstoqueId() {

        if (medicamentoComboBox != null && medicamentoComboBox.getValue() != null) {
            return medicamentoComboBox.getValue().getId();
        }

        if (medicamentoField != null) {
            String texto = medicamentoField.getText();

            if (texto != null && !texto.isBlank()) {
                try {
                    return Integer.valueOf(texto.trim());
                } catch (NumberFormatException e) {
                    mostrarErro("O ID do medicamento deve ser numérico.");
                    return null;
                }
            }
        }

        mostrarErro("Selecione o medicamento.");
        return null;
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
            Estoque est = historico.getEstoque();
            String nome = "";
            if (est != null) {
                if (est.getPrincipioAtivo() != null) {
                    nome = est.getPrincipioAtivo() + (est.getDose() != null ? " " + est.getDose() : "");
                } else {
                    nome = "Medicamento #" + est.getId();
                }
            }
            resumoItemLabel.setText(
                    nome + " | Qtd: " + historico.getQuantidade()
            );
        }
    }

    private void limparCampos() {

        if (medicamentoComboBox != null) {
            medicamentoComboBox.setValue(null);
            medicamentoComboBox.getSelectionModel().clearSelection();
        }

        if (medicamentoField != null) {
            medicamentoField.clear();
        }

        if (quantidadeField != null) {
            quantidadeField.clear();
        }

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