package com.gem.view;

import com.gem.controller.HistoricoController;
import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.control.TableCell;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class HistoricoViewController {

    // --- Labels do menu lateral ---

    @FXML
    private Label usuarioLabel;

    // --- Campos de filtro ---

    @FXML
    private ComboBox<String> tipoMovComboBox;

    @FXML
    private DatePicker dataInicioDatePicker;

    @FXML
    private DatePicker dataFimDatePicker;

    @FXML
    private TextField principioAtivoField;

    // --- Tabela e colunas ---

    @FXML
    private TableView<Historico> historicoTable;

    @FXML
    private TableColumn<Historico, String> tipoMovColumn;

    @FXML
    private TableColumn<Historico, String> medicamentoColumn;

    @FXML
    private TableColumn<Historico, Integer> quantidadeColumn;

    @FXML
    private TableColumn<Historico, String> dataHoraColumn;

    @FXML
    private TableColumn<Historico, String> profissionalColumn;

    @FXML
    private TableColumn<Historico, String> setorColumn;

    @FXML
    private TableColumn<Historico, Integer> qtdAntesColumn;

    @FXML
    private TableColumn<Historico, Integer> qtdDepoisColumn;

    // --- Dependências Spring ---

    private final HistoricoController historicoController;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public HistoricoViewController(
            HistoricoController historicoController,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager) {

        this.historicoController = historicoController;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private void initialize() {

        if (!sessaoUsuario.estaAutenticado()) {
            return;
        }

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        // Exibe o cargo no menu lateral
        usuarioLabel.setText(usuario.getCargo().name());

        configurarComboBoxTipo();
        configurarColunas();

        // Carrega o histórico completo ao abrir a tela
        carregarHistorico(null, null, null, null);
    }

    // -------------------------------------------------------
    // Ações dos botões do FXML
    // -------------------------------------------------------

    @FXML
    private void filtrar() {

        TipoMov tipoMov = obterTipoMovSelecionado();

        LocalDateTime dataInicio = dataInicioDatePicker.getValue() != null
                ? dataInicioDatePicker.getValue().atStartOfDay()
                : null;

        // A data fim vai até o último instante do dia selecionado
        LocalDateTime dataFim = dataFimDatePicker.getValue() != null
                ? dataFimDatePicker.getValue().atTime(23, 59, 59)
                : null;

        String principioAtivo = principioAtivoField.getText();

        carregarHistorico(tipoMov, dataInicio, dataFim, principioAtivo);
    }

    @FXML
    private void limpar() {

        tipoMovComboBox.setValue(null);
        dataInicioDatePicker.setValue(null);
        dataFimDatePicker.setValue(null);
        principioAtivoField.clear();

        carregarHistorico(null, null, null, null);
    }

    @FXML
    private void abrirMovimentacao() {

        try {

            Usuario usuario = sessaoUsuario.getUsuarioAtual();
            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            switch (usuario.getCargo()) {
                case COORDENADOR -> viewManager.mostrarCoordenador(stage);
                case FUNCIONARIO_CAF -> viewManager.mostrarMovimentacao(stage);
                case FUNCIONARIO_SAT -> viewManager.mostrarTecnico(stage);
            }

        } catch (Exception e) {

            e.printStackTrace();
            mostrarErro("Não foi possível abrir a movimentação.");
        }
    }

    @FXML
    private void abrirEstoque() {

        try {

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();
            viewManager.mostrarEstoque(stage);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarErro("Não foi possível abrir a tela de estoque.");
        }
    }

    @FXML
    private void sair() {

        sessaoUsuario.encerrar();

        try {

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            viewManager.mostrarLogin(stage);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarErro("Não foi possível voltar para o login.");
        }
    }

    // -------------------------------------------------------
    // Métodos privados de suporte
    // -------------------------------------------------------

    /**
     * Popula o ComboBox com as opções de tipo de movimentação.
     * A primeira opção ("Todos") equivale a filtro nulo.
     */
    private void configurarComboBoxTipo() {

        tipoMovComboBox.setItems(FXCollections.observableArrayList(
                "Todos",
                formatarTipoMov(TipoMov.SAIDA),
                formatarTipoMov(TipoMov.RETORNO),
                formatarTipoMov(TipoMov.ENTRADA)
        ));
    }

    /**
     * Configura cada coluna da TableView com a lógica de extração de dados
     * do objeto Historico. Colunas que precisam combinar campos de objetos
     * relacionados usam CellFactory em vez de PropertyValueFactory.
     */
    private void configurarColunas() {

        // Tipo de movimentação (formatado em português)
        tipoMovColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(formatarTipoMov(
                            ((Historico) getTableRow().getItem()).getTipoMov()
                    ));
                }
            }
        });

        // Medicamento: princípio ativo + dose (dados do Estoque relacionado)
        medicamentoColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Historico h = (Historico) getTableRow().getItem();
                    setText(h.getEstoque().getPrincipioAtivo()
                            + " - " + h.getEstoque().getDose());
                }
            }
        });

        // Quantidade
        quantidadeColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantidade")
        );

        // Data e hora formatadas
        dataHoraColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    LocalDateTime data = ((Historico) getTableRow().getItem()).getDataMov();
                    setText(data != null ? data.format(FORMATO_DATA_HORA) : "-");
                }
            }
        });

        // Profissional responsável
        profissionalColumn.setCellValueFactory(
                new PropertyValueFactory<>("nomeFunc")
        );

        // Setor de destino (formatado em português)
        setorColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    setText(formatarSetor(
                            ((Historico) getTableRow().getItem()).getSetorHosp()
                    ));
                }
            }
        });

        // Quantidade antes da movimentação
        qtdAntesColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantidadePre")
        );

        // Quantidade depois da movimentação
        qtdDepoisColumn.setCellValueFactory(
                new PropertyValueFactory<>("quantidadePos")
        );
    }

    /**
     * Consulta o histórico via controller e popula a TableView.
     */
    private void carregarHistorico(
            TipoMov tipoMov,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String principioAtivo) {

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        if (usuario == null) {
            mostrarErro("Usuário não autenticado.");
            return;
        }

        try {

            List<Historico> resultados = historicoController.consultar(
                    usuario, tipoMov, dataInicio, dataFim, principioAtivo
            );

            historicoTable.setItems(
                    FXCollections.observableArrayList(resultados)
            );

        } catch (Exception e) {

            e.printStackTrace();
            mostrarErro(e.getMessage() != null
                    ? e.getMessage()
                    : "Não foi possível carregar o histórico.");
        }
    }

    /**
     * Converte a seleção do ComboBox (String) de volta para o enum TipoMov.
     * Retorna null quando "Todos" estiver selecionado, o que desativa o filtro.
     */
    private TipoMov obterTipoMovSelecionado() {

        String selecionado = tipoMovComboBox.getValue();

        if (selecionado == null || selecionado.equals("Todos")) {
            return null;
        }

        for (TipoMov tipo : TipoMov.values()) {
            if (formatarTipoMov(tipo).equals(selecionado)) {
                return tipo;
            }
        }

        return null;
    }

    // -------------------------------------------------------
    // Formatadores
    // -------------------------------------------------------

    private String formatarTipoMov(TipoMov tipo) {

        return switch (tipo) {
            case SAIDA   -> "Saída";
            case RETORNO -> "Retorno";
            case ENTRADA -> "Entrada";
        };
    }

    private String formatarSetor(SetorHosp setor) {

        return switch (setor) {
            case SATELITE      -> "Satélite";
            case EMERGENCIA    -> "Emergência";
            case CLINICA_MEDICA -> "Clínica Médica";
            case PEDIATRIA     -> "Pediatria";
            case MATERNIDADE   -> "Maternidade";
            case ESTABILIZACAO -> "Estabilização";
        };
    }

    // -------------------------------------------------------
    // Diálogos
    // -------------------------------------------------------

    private void mostrarErro(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("GEM");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
