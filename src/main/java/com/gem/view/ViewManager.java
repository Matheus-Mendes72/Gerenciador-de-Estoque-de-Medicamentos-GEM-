package com.gem.view;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.gem.controller.RemoverMedicamentoController;
import com.gem.model.Medicamento;
import com.gem.security.SessaoUsuario;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@Component
public class ViewManager {

    private final ApplicationContext springContext;
    private final SessaoUsuario sessaoUsuario;

    public enum ScreenType {
        LOGIN,
        COORDENADOR,
        MOVIMENTACAO,
        TECNICO,
        ESTOQUE,
        HISTORICO,
        CADASTRO_MEDICAMENTO,
        REMOVER_MEDICAMENTO
    }

    private final Deque<ScreenType> historicoNavegacao = new ArrayDeque<>();
    private ScreenType telaAtual = null;

    // Estado de maximizado rastreado pelo ViewManager,
    // independente do que o Stage reporta durante transicoes de cena
    private boolean janelaMaximizada = false;

    public ViewManager(ApplicationContext springContext, SessaoUsuario sessaoUsuario) {
        this.springContext = springContext;
        this.sessaoUsuario = sessaoUsuario;
    }

    public void mostrarLogin(Stage stage) throws IOException {
        historicoNavegacao.clear();
        telaAtual = ScreenType.LOGIN;

        FXMLLoader loader = criarLoader("/view/TelaLogin.fxml");
        Parent root = loader.load();
        Scene scene = new Scene(root);

        configurarStage(stage, scene, "GEM - Acesso ao Sistema");
    }

    public void mostrarMovimentacao(Stage stage) throws IOException {
        mostrarMovimentacao(stage, true);
    }

    public void mostrarMovimentacao(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.MOVIMENTACAO, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaMovimentacao.fxml", "GEM - Movimentação de Medicamentos");
    }

    public void mostrarCoordenador(Stage stage) throws IOException {
        mostrarCoordenador(stage, true);
    }

    public void mostrarCoordenador(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.COORDENADOR, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaCoordenador.fxml", "GEM - Área do Coordenador");
    }

    public void mostrarTecnico(Stage stage) throws IOException {
        mostrarTecnico(stage, true);
    }

    public void mostrarTecnico(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.TECNICO, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaTecnico.fxml", "GEM - Área do Técnico de Enfermagem");
    }

    public void mostrarHistorico(Stage stage) throws IOException {
        mostrarHistorico(stage, true);
    }

    public void mostrarHistorico(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.HISTORICO, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaHistorico.fxml", "GEM - Histórico de Movimentações");
    }

    public void mostrarEstoque(Stage stage) throws IOException {
        mostrarEstoque(stage, true);
    }

    public void mostrarEstoque(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.ESTOQUE, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaEstoque.fxml", "GEM - Estoque de Medicamentos");
    }

    public void mostrarCadastroMedicamento(Stage stage) throws IOException {
        mostrarCadastroMedicamento(stage, true);
    }

    public void mostrarCadastroMedicamento(Stage stage, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.CADASTRO_MEDICAMENTO, registrarHistorico);
        carregarETrocarCena(stage, "/view/TelaCadastroMedicamento.fxml", "GEM - Cadastro de Medicamentos");
    }

    public void mostrarRemoverMedicamento(Stage stage) throws IOException {
        mostrarRemoverMedicamento(stage, null, true);
    }

    public void mostrarRemoverMedicamento(Stage stage, Medicamento medicamento) throws IOException {
        mostrarRemoverMedicamento(stage, medicamento, true);
    }

    public void mostrarRemoverMedicamento(Stage stage, Medicamento medicamento, boolean registrarHistorico) throws IOException {
        registrarNavegacao(ScreenType.REMOVER_MEDICAMENTO, registrarHistorico);

        FXMLLoader loader = criarLoader("/view/TelaRemoverMedicamento.fxml");
        Parent root = loader.load();

        if (medicamento != null) {
            RemoverMedicamentoController controller = loader.getController();
            controller.preselecionarMedicamento(medicamento);
        }

        Scene scene = new Scene(root);
        configurarStage(stage, scene, "GEM - Remoção de Medicamentos");
    }

    public void voltar(Stage stage) throws IOException {
        if (stage == null) {
            return;
        }

        if (!historicoNavegacao.isEmpty()) {
            ScreenType anterior = historicoNavegacao.pop();
            navegarPara(stage, anterior, false);
            return;
        }

        // Se o histórico estiver vazio, retorna para a tela inicial baseada no perfil autenticado
        if (sessaoUsuario != null && sessaoUsuario.estaAutenticado() && sessaoUsuario.getUsuarioAtual() != null && sessaoUsuario.getUsuarioAtual().getCargo() != null) {
            switch (sessaoUsuario.getUsuarioAtual().getCargo()) {
                case COORDENADOR -> mostrarCoordenador(stage, false);
                case FUNCIONARIO_CAF -> mostrarMovimentacao(stage, false);
                case FUNCIONARIO_SAT -> mostrarTecnico(stage, false);
            }
        } else {
            mostrarLogin(stage);
        }
    }

    private void registrarNavegacao(ScreenType novoTipo, boolean registrar) {
        if (registrar && telaAtual != null && telaAtual != novoTipo && telaAtual != ScreenType.LOGIN) {
            historicoNavegacao.push(telaAtual);
        }
        telaAtual = novoTipo;
    }

    private void navegarPara(Stage stage, ScreenType tela, boolean registrarHistorico) throws IOException {
        switch (tela) {
            case LOGIN -> mostrarLogin(stage);
            case COORDENADOR -> mostrarCoordenador(stage, registrarHistorico);
            case MOVIMENTACAO -> mostrarMovimentacao(stage, registrarHistorico);
            case TECNICO -> mostrarTecnico(stage, registrarHistorico);
            case ESTOQUE -> mostrarEstoque(stage, registrarHistorico);
            case HISTORICO -> mostrarHistorico(stage, registrarHistorico);
            case CADASTRO_MEDICAMENTO -> mostrarCadastroMedicamento(stage, registrarHistorico);
            case REMOVER_MEDICAMENTO -> mostrarRemoverMedicamento(stage, null, registrarHistorico);
        }
    }

    private void carregarETrocarCena(Stage stage, String fxmlPath, String titulo) throws IOException {
        FXMLLoader loader = criarLoader(fxmlPath);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        configurarStage(stage, scene, titulo);
    }

    private void configurarStage(Stage stage, Scene scene, String titulo) {
        stage.setScene(scene);
        stage.setTitle(titulo);
        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        if (!stage.isShowing()) {
            // Primeira exibicao: mostra a janela normalmente
            stage.show();
        } else if (janelaMaximizada) {
            // Janela ja visivel e estava maximizada: reaplica sem chamar show()
            // pois show() em janela UNDECORATED ja visivel reseta o estado no Windows
            stage.setMaximized(false);
            stage.setMaximized(true);
        }
    }

    // -------------------------------------------------------------
    // Controles de Janela
    // -------------------------------------------------------------

    public void minimizar(Stage stage) {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    public void alternarMaximizar(Stage stage) {
        if (stage != null) {
            janelaMaximizada = !janelaMaximizada;
            stage.setMaximized(janelaMaximizada);
        }
    }

    public void fechar(Stage stage) {
        if (stage != null) {
            stage.close();
        }
    }

    private FXMLLoader criarLoader(String caminho) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(caminho));
        loader.setControllerFactory(springContext::getBean);
        return loader;
    }
}
