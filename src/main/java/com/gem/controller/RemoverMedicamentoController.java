package com.gem.controller;

import com.gem.model.Medicamento;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.RemoverMedicamentoService;
import com.gem.view.ViewManager;

import java.util.List;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.springframework.stereotype.Controller;

@Controller
public class RemoverMedicamentoController {

    private static final int QUANTIDADE_MAX_DIGITOS = 9;
    private static final int[] SUGESTOES_QUANTIDADE = {1, 5, 10, 20, 50, 100};

    private final RemoverMedicamentoService removerMedicamentoService;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    /** Saldo total do medicamento atualmente selecionado (soma de todos os lotes). */
    private int quantidadeDisponivelAtual = 0;

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
        configurarCampoQuantidade();
        configurarCampoMotivo();
        configurarExibicaoMedicamentos();
        configurarSelecaoMedicamento();
        carregarUsuario();
        carregarMedicamentos();
    }

    /**
     * CORREÇÃO DO BUG: o ComboBox<Integer> era editável sem StringConverter.
     * Ao digitar um número, o JavaFX guardava um String dentro do ComboBox<Integer>,
     * e o getValue() lançava ClassCastException (engolida pelo handler do botão),
     * fazendo a remoção "não acontecer". Agora há um conversor Integer <-> String
     * e o campo aceita apenas dígitos.
     */
    private void configurarCampoQuantidade() {
        if (campoQuantidade == null) return;

        campoQuantidade.setEditable(true);

        campoQuantidade.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer valor) {
                return valor == null ? "" : valor.toString();
            }

            @Override
            public Integer fromString(String texto) {
                if (texto == null || texto.isBlank()) {
                    return null;
                }
                try {
                    return Integer.valueOf(texto.trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });

        UnaryOperator<TextFormatter.Change> somenteDigitos = change ->
                change.getControlNewText().matches("\\d{0," + QUANTIDADE_MAX_DIGITOS + "}")
                        ? change
                        : null;

        campoQuantidade.getEditor().setTextFormatter(new TextFormatter<>(somenteDigitos));
    }

    private void configurarCampoMotivo() {
        if (campoMotivo == null) return;

        // O campo motivo_obs do histórico aceita no máximo 100 caracteres.
        UnaryOperator<TextFormatter.Change> limitarTamanho = change ->
                change.getControlNewText().length() <= RemoverMedicamentoService.MOTIVO_MAX_LENGTH
                        ? change
                        : null;

        campoMotivo.setTextFormatter(new TextFormatter<>(limitarTamanho));
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
                .addListener((observable, anterior, selecionado) -> atualizarQuantidades());
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

    private void limparCampoQuantidade() {
        if (campoQuantidade == null) return;

        campoQuantidade.getItems().clear();
        campoQuantidade.setValue(null);
        campoQuantidade.getEditor().clear();
        campoQuantidade.setDisable(false);
        campoQuantidade.setPromptText("Qtd. a remover");
        quantidadeDisponivelAtual = 0;
    }

    /**
     * Antes a lista era preenchida com 1..N (e o 1 vinha pré-selecionado), o que
     * mascarava o problema e travava a tela com estoques grandes. Agora são
     * oferecidas apenas sugestões e o total disponível; o usuário informa o valor.
     */
    private void atualizarQuantidades() {
        if (campoQuantidade == null || campoMedicamento == null) return;

        limparCampoQuantidade();

        Medicamento selecionado =
                campoMedicamento.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            return;
        }

        try {
            int disponivel =
                    removerMedicamentoService.obterQuantidadeDisponivel(selecionado);

            quantidadeDisponivelAtual = disponivel;

            if (disponivel <= 0) {
                campoQuantidade.setDisable(true);
                campoQuantidade.setPromptText("Sem saldo em estoque");
                return;
            }

            TreeSet<Integer> opcoes = new TreeSet<>();
            for (int sugestao : SUGESTOES_QUANTIDADE) {
                if (sugestao <= disponivel) {
                    opcoes.add(sugestao);
                }
            }
            opcoes.add(disponivel);

            campoQuantidade.getItems().setAll(opcoes);
            campoQuantidade.setPromptText("Máx.: " + disponivel);

        } catch (RuntimeException e) {
            exibirAlerta(
                    AlertType.ERROR,
                    "Erro ao consultar estoque",
                    "Não foi possível consultar a quantidade disponível."
            );
        }
    }

    /**
     * Lê a quantidade a partir do texto digitado (fonte de verdade), com
     * fallback para o item selecionado na lista.
     *
     * @throws NumberFormatException se o texto não for um inteiro válido
     */
    private Integer lerQuantidadeInformada() {
        String texto = campoQuantidade.getEditor() != null
                ? campoQuantidade.getEditor().getText()
                : null;

        if (texto != null && !texto.isBlank()) {
            return Integer.valueOf(texto.trim());
        }

        return campoQuantidade.getValue();
    }

    @FXML
    public void remover() {
        if (campoMedicamento == null) return;

        Medicamento medicamento =
                campoMedicamento.getSelectionModel().getSelectedItem();

        if (medicamento == null) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Medicamento não selecionado",
                    "Selecione o medicamento que deseja remover."
            );
            return;
        }

        String motivo = (campoMotivo != null && campoMotivo.getText() != null)
                ? campoMotivo.getText().trim()
                : "";

        if (motivo.isBlank()) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Motivo obrigatório",
                    "Informe o motivo da remoção antes de confirmar."
            );
            return;
        }

        // Medicamento ativo, porém sem saldo: só é possível retirá-lo do catálogo.
        if (quantidadeDisponivelAtual <= 0) {
            removerSemSaldoDoCatalogo(medicamento, motivo);
            return;
        }

        Integer quantidadeParaRemover;
        try {
            quantidadeParaRemover = lerQuantidadeInformada();
        } catch (NumberFormatException e) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Quantidade inválida",
                    "Informe uma quantidade numérica válida."
            );
            return;
        }

        if (quantidadeParaRemover == null || quantidadeParaRemover <= 0) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Quantidade não informada",
                    "Selecione ou digite a quantidade que deseja remover."
            );
            return;
        }

        if (quantidadeParaRemover > quantidadeDisponivelAtual) {
            exibirAlerta(
                    AlertType.WARNING,
                    "Quantidade maior que o estoque",
                    "A quantidade informada excede o saldo. Disponível: " + quantidadeDisponivelAtual + "."
            );
            return;
        }

        boolean remocaoTotal = quantidadeParaRemover == quantidadeDisponivelAtual;

        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção");
        confirmacao.setHeaderText("Confirma a remoção da quantidade informada?");
        confirmacao.setContentText(
                "Medicamento: " + medicamento.getPrincipioAtivo() + " (" + medicamento.getDose() + ")\n"
                + "Quantidade a remover: " + quantidadeParaRemover + " de " + quantidadeDisponivelAtual + "\n"
                + "Motivo: " + motivo
                + (remocaoTotal ? "\n\nO saldo será zerado e o medicamento será inativado no catálogo." : "")
        );

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            removerMedicamentoService.removerEstoque(
                    medicamento,
                    quantidadeParaRemover,
                    motivo
            );

            exibirAlerta(
                    AlertType.INFORMATION,
                    "Remoção realizada",
                    "Foram removidas " + quantidadeParaRemover + " unidade(s) do medicamento com sucesso!"
            );

            resetarFormulario();

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

    private void removerSemSaldoDoCatalogo(Medicamento medicamento, String motivo) {
        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar remoção do catálogo");
        confirmacao.setHeaderText("Este medicamento não possui saldo em estoque.");
        confirmacao.setContentText(
                "Medicamento: " + medicamento.getPrincipioAtivo() + " (" + medicamento.getDose() + ")\n"
                + "Motivo: " + motivo + "\n\n"
                + "Deseja inativá-lo do catálogo?"
        );

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            removerMedicamentoService.removerDoCatalogo(medicamento.getId(), motivo);

            exibirAlerta(
                    AlertType.INFORMATION,
                    "Remoção realizada",
                    "Medicamento removido do catálogo com sucesso!"
            );

            resetarFormulario();

        } catch (RuntimeException e) {
            exibirAlerta(
                    AlertType.ERROR,
                    "Erro na remoção",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Não foi possível remover o medicamento do catálogo."
            );
        }
    }

    private void resetarFormulario() {
        campoMedicamento.getSelectionModel().clearSelection();
        limparCampoQuantidade();
        if (campoMotivo != null) campoMotivo.clear();
        carregarMedicamentos();
    }

    public void preselecionarMedicamento(Medicamento medicamento) {
        if (medicamento == null || medicamento.getId() == null || campoMedicamento == null) return;

        campoMedicamento.getItems().stream()
                .filter(m -> medicamento.getId().equals(m.getId()))
                .findFirst()
                .ifPresent(m -> campoMedicamento.getSelectionModel().select(m));
    }

    @FXML
    public void voltar() {
        try {
            Stage stage = (Stage) campoMedicamento.getScene().getWindow();
            if (stage != null) {
                viewManager.voltar(stage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void minimizarJanela() {
        Stage stage = (Stage) campoMedicamento.getScene().getWindow();
        if (stage != null) {
            viewManager.minimizar(stage);
        }
    }

    @FXML
    private void maximizarJanela() {
        Stage stage = (Stage) campoMedicamento.getScene().getWindow();
        if (stage != null) {
            viewManager.alternarMaximizar(stage);
        }
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) campoMedicamento.getScene().getWindow();
        if (stage != null) {
            viewManager.fechar(stage);
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