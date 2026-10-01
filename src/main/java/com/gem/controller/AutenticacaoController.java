package com.gem.controller;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.AutenticacaoService;
import org.springframework.stereotype.Controller;

@Controller
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final SessaoUsuario sessaoUsuario;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            SessaoUsuario sessaoUsuario
    ) {
        this.autenticacaoService = autenticacaoService;
        this.sessaoUsuario = sessaoUsuario;
    }

    public Usuario entrar(
            CargoProfissional cargo,
            String senha
    ) {

        Usuario usuario =
                autenticacaoService.autenticar(cargo, senha);

        sessaoUsuario.iniciar(usuario);

        return usuario;
    }
}