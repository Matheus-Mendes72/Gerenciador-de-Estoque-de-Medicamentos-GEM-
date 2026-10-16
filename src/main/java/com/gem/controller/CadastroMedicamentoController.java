
package com.gem.controller;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.model.TipoMed;
import com.gem.service.MedicamentoService;

import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import org.springframework.stereotype.Controller;

@Controller
public class CadastroMedicamentoController {

    private final MedicamentoService medicamentoService;

    public CadastroMedicamentoController(
            MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
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
    private DatePicker campoDataValidade;

    @FXML
    private TextField campoQuantidade;

    @FXML
    private TextField campoLote;

    @FXML
    public void initialize() {
        campoTipoMed.getItems().setAll(TipoMed.values());
    }

    @FXML
    public void cadastrar() {

        // 1. Validar os campos obrigatórios
        if (campoPrincipioAtivo.getText().isBlank()
                || campoDose.getText().isBlank()
                || campoTipoMed.getValue() == null
                || campoLaboratorio.getText().isBlank()
                || campoDataValidade.getValue() == null
                || campoQuantidade.getText().isBlank()
                || campoLote.getText().isBlank()) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos obrigatórios",
                    "Preencha todos os campos antes de salvar."
            );
            return;
        }

        // 2. Validar a quantidade
        int quantidade;

        try {
            quantidade = Integer.parseInt(
                    campoQuantidade.getText().trim()
            );

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

        // 3. Criar o objeto Medicamento
        Medicamento medicamento = new Medicamento();

        medicamento.setPrincipioAtivo(
                campoPrincipioAtivo.getText().trim()
        );
        medicamento.setDose(campoDose.getText().trim());
        medicamento.setTipoMed(campoTipoMed.getValue());
        // medicamento.setLaboratorio(
        //         campoLaboratorio.getText().trim()
        // );
        medicamento.setAtivo(true);

        // 4. Criar o objeto Estoque
        Estoque estoque = new Estoque();

        // estoque.setLote(campoLote.getText().trim());
        // estoque.setDataValidade(campoDataValidade.getValue());
        estoque.setQuantidade(quantidade);

        // 5. Salvar os dados
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
                    "Não foi possível cadastrar o medicamento."
            );

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

    private void limparCampos() {
        campoPrincipioAtivo.clear();
        campoDose.clear();
        campoTipoMed.setValue(null);
        campoLaboratorio.clear();
        campoDataValidade.setValue(null);
        campoQuantidade.clear();
        campoLote.clear();
    }
}
