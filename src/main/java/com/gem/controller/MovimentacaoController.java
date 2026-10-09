package com.gem.controller;

import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.Usuario;
import com.gem.security.SessaoUsuario;
import com.gem.service.MovimentacaoService;

import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final SessaoUsuario sessaoUsuario;

    public MovimentacaoController(
            MovimentacaoService movimentacaoService,
            SessaoUsuario sessaoUsuario) {

        this.movimentacaoService = movimentacaoService;
        this.sessaoUsuario = sessaoUsuario;
    }

    public boolean podeAcessarRetorno() {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        return usuario != null && movimentacaoService.podeAcessarRetorno(usuario);
    }

    public boolean podeAcessarRetorno(Usuario usuario) {
        return movimentacaoService.podeAcessarRetorno(usuario);
    }

    public void validarAcessoRetorno() {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        if (usuario == null) {
            throw new SecurityException("Acesso negado: nenhum usuário autenticado.");
        }
        movimentacaoService.validarAcessoRetorno(usuario);
    }

    public void validarAcessoRetorno(Usuario usuario) {
        movimentacaoService.validarAcessoRetorno(usuario);
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
            String motivo) {

        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não autenticado na sessão.");
        }

        return movimentacaoService.registrarRetorno(
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

    public Historico registrarRetorno(
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            Usuario usuario,
            String identificacaoProfissional) {

        return movimentacaoService.registrarRetorno(
                usuario,
                estoqueId,
                quantidade,
                setor,
                motivo,
                identificacaoProfissional
        );
    }

    public List<Historico> listarRetornosDoUsuarioAtual() {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        return movimentacaoService.listarRetornosPorUsuario(usuario);
    }
}