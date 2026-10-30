package com.gem.view;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.security.SessaoUsuario;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EstoqueViewController {

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label headerNomeLabel;

    @FXML
    private Label headerCargoLabel;

    @FXML
    private Label headerAvatarLabel;

    @FXML
    private Label sidebarAvatarLabel;

    @FXML
    private Label totalMedicamentosLabel;

    @FXML
    private TextField campoBusca;

    @FXML
    private TableView<Estoque> estoqueTable;

    @FXML
    private TableColumn<Estoque, String> medicamentoColumn;

    @FXML
    private TableColumn<Estoque, String> categoriaColumn;

    @FXML
    private TableColumn<Estoque, String> estoqueColumn;

    @FXML
    private TableColumn<Estoque, String> loteColumn;

    @FXML
    private TableColumn<Estoque, Estoque> statusColumn;

    @FXML
    private TableColumn<Estoque, Estoque> acoesColumn;

    @FXML
    private Button btnCadastrarMedicamento;

    @FXML
    private Button btnHistorico;

    private final EstoqueRepository estoqueRepository;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    private final ObservableList<Estoque> listaEstoque = FXCollections.observableArrayList();
    private FilteredList<Estoque> listaFiltrada;

    public EstoqueViewController(
            EstoqueRepository estoqueRepository,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager) {
        this.estoqueRepository = estoqueRepository;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private void initialize() {
        carregarUsuario();
        configurarColunas();
        carregarDados();
        configurarFiltroBusca();
    }

    private void carregarUsuario() {
        if (!sessaoUsuario.estaAutenticado()) {
            return;
        }

        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        if (usuario == null || usuario.getCargo() == null) {
            return;
        }

        com.gem.model.CargoProfissional cargoEnum = usuario.getCargo();
        String cargo = cargoEnum.name();

        if (usuarioLabel != null) {
            usuarioLabel.setText(cargo);
        }

        if (headerNomeLabel != null) {
            headerNomeLabel.setText(formatarCargo(cargo));
        }

        if (headerCargoLabel != null) {
            headerCargoLabel.setText(switch (cargoEnum) {
                case COORDENADOR -> "Coordenação Geral";
                case FUNCIONARIO_CAF -> "Farmácia Hospitalar";
                case FUNCIONARIO_SAT -> "Técnico de Enfermagem";
            });
        }

        if (headerAvatarLabel != null) {
            headerAvatarLabel.setText(obterIniciais(cargo));
        }

        if (sidebarAvatarLabel != null) {
            sidebarAvatarLabel.setText(obterIniciais(cargo));
        }

        // Se o usuário não for Coordenador, oculta ações de cadastro, remoção e histórico
        boolean isCoordenador = cargoEnum == com.gem.model.CargoProfissional.COORDENADOR;

        if (btnCadastrarMedicamento != null) {
            btnCadastrarMedicamento.setVisible(isCoordenador);
            btnCadastrarMedicamento.setManaged(isCoordenador);
        }

        if (btnHistorico != null) {
            btnHistorico.setVisible(isCoordenador);
            btnHistorico.setManaged(isCoordenador);
        }

        if (acoesColumn != null) {
            acoesColumn.setVisible(isCoordenador);
        }
    }

    private void configurarColunas() {
        // Coluna Medicamento (Princípio Ativo + Dose)
        medicamentoColumn.setCellValueFactory(cellData -> {
            Estoque e = cellData.getValue();
            String nome = e.getPrincipioAtivo() != null ? e.getPrincipioAtivo() : "Sem nome";
            String dose = e.getDose() != null ? e.getDose() : "";
            return new SimpleStringProperty(nome + (dose.isBlank() ? "" : " " + dose));
        });

        // Coluna Categoria
        categoriaColumn.setCellValueFactory(cellData -> {
            Estoque e = cellData.getValue();
            String cat = e.getTipoMed() != null ? formatarTipoMed(e.getTipoMed().name()) : "Geral";
            return new SimpleStringProperty(cat);
        });

        // Coluna Quantidade Estoque
        estoqueColumn.setCellValueFactory(cellData -> {
            Estoque e = cellData.getValue();
            int qtd = e.getQuantidade() != null ? e.getQuantidade() : 0;
            return new SimpleStringProperty(qtd + " un.");
        });

        // Coluna Lote
        loteColumn.setCellValueFactory(cellData -> {
            Estoque e = cellData.getValue();
            String lote = e.getLote() != null && !e.getLote().isBlank()
                    ? "Lote " + e.getLote()
                    : "Lote Geral";
            return new SimpleStringProperty(lote);
        });

        // Coluna Status com pill badges coloridos
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Estoque item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    int qtd = item.getQuantidade() != null ? item.getQuantidade() : 0;
                    Label pill = new Label();
                    if (qtd > 50) {
                        pill.setText("Normal");
                        pill.getStyleClass().add("pill-status-normal");
                    } else if (qtd >= 20) {
                        pill.setText("Atenção");
                        pill.getStyleClass().add("pill-status-atencao");
                    } else {
                        pill.setText("Baixo");
                        pill.getStyleClass().add("pill-status-baixo");
                    }

                    HBox container = new HBox(pill);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Coluna Ações: botão Remover
    acoesColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    acoesColumn.setCellFactory(col -> new TableCell<>() {

        private final Button btnRemover = new Button("Remover");

        {
            btnRemover.getStyleClass().add("action-remover-link");
            btnRemover.setOnAction(event -> {
                Estoque item = getItem();
                abrirRemoverMedicamento(item != null ? item.getMedicamento() : null);
            });
        }

        @Override
        protected void updateItem(Estoque item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                HBox box = new HBox(btnRemover);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        }
        });
    }

    private void carregarDados() {
        try {
            List<Estoque> itens = estoqueRepository.findAllAtivos();
            listaEstoque.setAll(itens);

            listaFiltrada = new FilteredList<>(listaEstoque, p -> true);
            estoqueTable.setItems(listaFiltrada);

            atualizarContador();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível carregar os dados de estoque.");
        }
    }

    private void configurarFiltroBusca() {
        if (campoBusca != null) {
            campoBusca.textProperty().addListener((observable, oldValue, newValue) -> {
                if (listaFiltrada == null) return;

                listaFiltrada.setPredicate(estoque -> {
                    if (newValue == null || newValue.isBlank()) {
                        return true;
                    }

                    String filtro = newValue.toLowerCase().trim();

                    String principioAtivo = estoque.getPrincipioAtivo() != null
                            ? estoque.getPrincipioAtivo().toLowerCase() : "";
                    String dose = estoque.getDose() != null
                            ? estoque.getDose().toLowerCase() : "";
                    String lote = estoque.getLote() != null
                            ? estoque.getLote().toLowerCase() : "";
                    String tipo = estoque.getTipoMed() != null
                            ? estoque.getTipoMed().name().toLowerCase() : "";

                    return principioAtivo.contains(filtro)
                            || dose.contains(filtro)
                            || lote.contains(filtro)
                            || tipo.contains(filtro);
                });

                atualizarContador();
            });
        }
    }

    private void atualizarContador() {
        if (totalMedicamentosLabel != null && listaFiltrada != null) {
            int total = listaFiltrada.size();
            totalMedicamentosLabel.setText(total + (total == 1 ? " medicamento encontrado" : " medicamentos encontrados"));
        }
    }

    // -------------------------------------------------------------------------
    // Ações de Navegação
    // -------------------------------------------------------------------------

    @FXML
    public void abrirCadastroMedicamento() {
        try {
            Stage stage = (Stage) estoqueTable.getScene().getWindow();
            viewManager.mostrarCadastroMedicamento(stage);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível abrir o cadastro de medicamentos.");
        }
    }

    @FXML
    public void abrirRemoverMedicamento() {
        abrirRemoverMedicamento(null);
    }

    private void abrirRemoverMedicamento(Medicamento medicamento) {
        try {
            Stage stage = (Stage) estoqueTable.getScene().getWindow();
            viewManager.mostrarRemoverMedicamento(stage, medicamento);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível abrir a remoção de medicamentos.");
        }
    }


    @FXML
    public void abrirMovimentacao() {
        try {
            Stage stage = (Stage) estoqueTable.getScene().getWindow();
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
            mostrarErro("Não foi possível abrir a movimentação.");
        }
    }

    @FXML
    public void abrirHistorico() {
        try {
            Stage stage = (Stage) estoqueTable.getScene().getWindow();
            viewManager.mostrarHistorico(stage);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível abrir o histórico.");
        }
    }

    @FXML
    public void sair() {
        try {
            sessaoUsuario.encerrar();
            Stage stage = (Stage) estoqueTable.getScene().getWindow();
            viewManager.mostrarLogin(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatarCargo(String cargo) {
        if (cargo == null) return "Usuário";
        return switch (cargo) {
            case "COORDENADOR" -> "Coordenador";
            case "FUNCIONARIO_CAF" -> "Farmacêutico";
            case "FUNCIONARIO_SAT" -> "Técnico de Enfermagem";
            default -> cargo;
        };
    }

    private String obterIniciais(String texto) {
        if (texto == null || texto.isBlank()) return "GE";
        if (texto.contains("_")) {
            String[] partes = texto.split("_");
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return texto.length() >= 2 ? texto.substring(0, 2).toUpperCase() : texto.toUpperCase();
    }

    private String formatarTipoMed(String tipo) {
        return switch (tipo) {
            case "CONTROLADO" -> "Controlado";
            case "ANTIBIOTICO" -> "Antibiótico";
            case "COMUM" -> "Comum";
            default -> tipo;
        };
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("GEM");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
