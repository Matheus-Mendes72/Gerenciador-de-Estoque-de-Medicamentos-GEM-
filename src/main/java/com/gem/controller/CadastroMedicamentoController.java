package com.gem.controller;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.model.TipoMed;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.MedicamentoService;
import com.gem.view.ViewManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.springframework.stereotype.Controller;

@Controller
public class CadastroMedicamentoController {

    private final MedicamentoService medicamentoService;
    private final SessaoUsuario sessaoUsuario;
    private final ViewManager viewManager;

    public CadastroMedicamentoController(
            MedicamentoService medicamentoService,
            SessaoUsuario sessaoUsuario,
            ViewManager viewManager) {
        this.medicamentoService = medicamentoService;
        this.sessaoUsuario = sessaoUsuario;
        this.viewManager = viewManager;
    }

    @FXML
    private TextField campoPrincipioAtivo;

    @FXML
    private TextField campoDose;

    @FXML
    private ComboBox<TipoMed> campoTipoMed;

    @FXML
    private TextField campoLaboratorio;

    @FXML
    private TextField campoQuantidade;

    @FXML
    private TextField campoLote;

    @FXML
    private TextField campoResponsavel;

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label acessoLabel;

    @FXML
    private Label resumoResponsavelLabel;

    @FXML
    private Label resumoPerfilLabel;

    @FXML
    public void initialize() {
        if (campoTipoMed != null) {
            campoTipoMed.getItems().setAll(TipoMed.values());
        }
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        if (usuario != null && usuario.getCargo() != null) {
            String cargo = usuario.getCargo().name();
            if (usuarioLabel != null) usuarioLabel.setText(cargo);
            if (acessoLabel != null) acessoLabel.setText("Acesso: " + cargo);
            if (campoResponsavel != null) campoResponsavel.setText(cargo);
            if (resumoResponsavelLabel != null) resumoResponsavelLabel.setText(cargo);
            if (resumoPerfilLabel != null) resumoPerfilLabel.setText(cargo);
        }
    }

    @FXML
    public void cadastrar() {

        // 1. Validar todos os campos obrigatórios especificados na US-08
        if (campoPrincipioAtivo.getText() == null || campoPrincipioAtivo.getText().isBlank()
                || campoDose.getText() == null || campoDose.getText().isBlank()
                || campoTipoMed.getValue() == null
                || campoLaboratorio.getText() == null || campoLaboratorio.getText().isBlank()
                || campoQuantidade.getText() == null || campoQuantidade.getText().isBlank()
                || campoLote.getText() == null || campoLote.getText().isBlank()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos obrigatórios",
                    "Preencha todos os campos obrigatórios antes de salvar."
            );
            return;
        }

        // 2. Validar a quantidade inicial
        int quantidade;
        try {
            quantidade = Integer.parseInt(campoQuantidade.getText().trim());

            if (quantidade <= 0) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Quantidade inválida",
                        "A quantidade deve ser maior que zero."
                );
                return;
            }

        } catch (NumberFormatException e) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Quantidade inválida",
                    "Informe uma quantidade usando números inteiros."
            );
            return;
        }

        // 3. Criar o objeto Medicamento com os dados informados
        Medicamento medicamento = new Medicamento();
        medicamento.setPrincipioAtivo(campoPrincipioAtivo.getText().trim());
        medicamento.setDose(campoDose.getText().trim());
        medicamento.setTipoMed(campoTipoMed.getValue());
        medicamento.setLaboratorio(campoLaboratorio.getText().trim());
        medicamento.setAtivo(true);

        // 4. Criar o objeto Estoque inicial com lote
        Estoque estoque = new Estoque();
        estoque.setLote(campoLote.getText().trim());
        estoque.setQuantidade(quantidade);

        // 5. Salvar os dados via serviço
        try {
            medicamentoService.cadastrarComEstoque(
                    medicamento,
                    estoque
            );

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Cadastro realizado",
                    "Medicamento cadastrado com sucesso!"
            );

            limparCampos();

        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro no cadastro",
                    e.getMessage() != null ? e.getMessage() : "Não foi possível cadastrar o medicamento."
            );
            e.printStackTrace();
        }
    }

    @FXML
    public void limparCampos() {
        if (campoPrincipioAtivo != null) campoPrincipioAtivo.clear();
        if (campoDose != null) campoDose.clear();
        if (campoTipoMed != null) campoTipoMed.setValue(null);
        if (campoLaboratorio != null) campoLaboratorio.clear();
        if (campoQuantidade != null) campoQuantidade.clear();
        if (campoLote != null) campoLote.clear();
    }

    @FXML
    public void voltar() {
        try {
            Stage stage = (Stage) campoPrincipioAtivo.getScene().getWindow();
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
            Stage stage = (Stage) campoPrincipioAtivo.getScene().getWindow();
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
            Stage stage = (Stage) campoPrincipioAtivo.getScene().getWindow();
            viewManager.mostrarHistorico(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirRemoverMedicamento() {
        try {
            Stage stage = (Stage) campoPrincipioAtivo.getScene().getWindow();
            viewManager.mostrarRemoverMedicamento(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sair() {
        try {
            sessaoUsuario.encerrar();
            Stage stage = (Stage) campoPrincipioAtivo.getScene().getWindow();
            viewManager.mostrarLogin(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
