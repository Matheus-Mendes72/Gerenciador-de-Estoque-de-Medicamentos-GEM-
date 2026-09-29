package com.gem.controller;

import org.springframework.stereotype.Controller;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.service.AutenticacaoService;

@Controller 
public class AutenticacaoController {
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    public Usuario entrar(CargoProfissional cargo, String senha) {
        return autenticacaoService.autenticar(cargo, senha);
    }
}
