package com.gem.controller;

import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;

@Component
public class CoordenadorController {

    private final SessaoUsuario sessaoUsuario;

    public CoordenadorController(SessaoUsuario sessaoUsuario) {
        this.sessaoUsuario = sessaoUsuario;
    }

    @FXML
    private void abrirCadastro() {
        validarCoordenador();

        // abrir tela de cadastro
    }

    @FXML
    private void abrirAtualizacao() {
        validarCoordenador();

        // abrir tela de atualização
    }

    @FXML
    private void abrirRemocao() {
        validarCoordenador();

        // abrir tela de remoção
    }

    @FXML
    private void abrirSaida() {
        validarCoordenador();

        // abrir tela de saída
    }

    @FXML
    private void abrirRetorno() {
        validarCoordenador();

        // abrir tela de retorno
    }

    private void validarCoordenador() {

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        if (usuario == null ||
            usuario.getCargo() != CargoProfissional.COORDENADOR) {

            throw new SecurityException(
                "Acesso permitido somente ao coordenador."
            );
        }
    }
}