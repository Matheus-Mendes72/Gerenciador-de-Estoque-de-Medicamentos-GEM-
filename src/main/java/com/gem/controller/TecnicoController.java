package com.gem.controller;

import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;

@Component
public class TecnicoController {

    private final SessaoUsuario sessaoUsuario;

    public TecnicoController(SessaoUsuario sessaoUsuario) {
        this.sessaoUsuario = sessaoUsuario;
    }

    @FXML
    private void abrirSaida() {
        validarTecnico();

        // abrir tela de saída
    }

    @FXML
    private void abrirRetorno() {
        validarTecnico();

        // abrir tela de retorno
    }

    private void validarTecnico() {

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        if (usuario == null
                || usuario.getCargo() != CargoProfissional.FUNCIONARIO_SAT) {

            throw new SecurityException(
                    "Acesso permitido somente ao técnico de enfermagem."
            );
        }
    }
}