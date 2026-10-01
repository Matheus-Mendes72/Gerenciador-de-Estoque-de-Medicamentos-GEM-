package com.gem.controller;

import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.Usuario;
import com.gem.service.MovimentacaoService;

import org.springframework.stereotype.Controller;

@Controller
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(
            MovimentacaoService movimentacaoService) {

        this.movimentacaoService = movimentacaoService;
    }

    public Historico registrarSaida(
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            Usuario usuario) {

        return movimentacaoService.registrarSaida(
                usuario,
                estoqueId,
                quantidade,
                setor,
                motivo
        );
    }

    public Historico registrarRetorno(
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            Usuario usuario) {

        return movimentacaoService.registrarRetorno(
                usuario,
                estoqueId,
                quantidade,
                setor,
                motivo
        );
    }
}