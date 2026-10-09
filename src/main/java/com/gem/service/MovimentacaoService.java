package com.gem.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.Permissao;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.model.SetorHosp;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;

@Service
public class MovimentacaoService {

    private final EstoqueRepository estoqueRepository;
    private final HistoricoRepository historicoRepository;
    private final PermissaoService permissaoService;

    public MovimentacaoService(
            EstoqueRepository estoqueRepository,
            HistoricoRepository historicoRepository,
            PermissaoService permissaoService) {

        this.estoqueRepository = estoqueRepository;
        this.historicoRepository = historicoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public Historico registrarSaida(
            Usuario usuario,
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REGISTRAR_SAIDA
        );

        Estoque estoque = buscarEstoque(estoqueId);

        validarQuantidade(quantidade);

        if (estoque.getQuantidade() < quantidade) {
            throw new IllegalArgumentException(
                    "Quantidade disponível insuficiente."
            );
        }

        int quantidadePre = estoque.getQuantidade();

        int quantidadePos =
                quantidadePre - quantidade;

        estoque.setQuantidade(quantidadePos);

        estoqueRepository.save(estoque);

        Historico historico = criarHistorico(
                usuario,
                estoque,
                TipoMov.SAIDA,
                quantidade,
                quantidadePre,
                quantidadePos,
                setor,
                motivo
        );

        return historicoRepository.save(historico);
    }

    public boolean podeAcessarRetorno(Usuario usuario) {
        return permissaoService.podeAcessarRetorno(usuario);
    }

    public void validarAcessoRetorno(Usuario usuario) {
        permissaoService.exigirAcessoRetorno(usuario);
    }

    @Transactional
    public Historico registrarRetorno(
            Usuario usuario,
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo) {

        return registrarRetorno(usuario, estoqueId, quantidade, setor, motivo, null);
    }

    @Transactional
    public Historico registrarRetorno(
            Usuario usuario,
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            String identificacaoProfissional) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Usuário não autenticado."
            );
        }

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REGISTRAR_RETORNO
        );

        if (setor == null) {
            throw new IllegalArgumentException(
                    "O setor de origem do retorno é obrigatório."
            );
        }

        Estoque estoque = buscarEstoque(estoqueId);

        validarQuantidade(quantidade);

        int quantidadePre = estoque.getQuantidade();

        int quantidadePos =
                quantidadePre + quantidade;

        estoque.setQuantidade(quantidadePos);

        estoqueRepository.save(estoque);

        Historico historico = criarHistorico(
                usuario,
                estoque,
                TipoMov.RETORNO,
                quantidade,
                quantidadePre,
                quantidadePos,
                setor,
                motivo,
                identificacaoProfissional
        );

        return historicoRepository.save(historico);
    }

    public java.util.List<Historico> listarRetornosPorUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return java.util.Collections.emptyList();
        }
        return historicoRepository.findByTipoMovAndUsuarioIdOrderByDataMovDesc(
                TipoMov.RETORNO,
                usuario.getId()
        );
    }

    public java.util.List<Historico> listarTodosRetornos() {
        return historicoRepository.findByTipoMovOrderByDataMovDesc(
                TipoMov.RETORNO
        );
    }

    private Estoque buscarEstoque(Integer id) {

        return estoqueRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Medicamento não encontrado."
                    )
                );
    }

    private void validarQuantidade(Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero."
            );
        }
    }

    private Historico criarHistorico(
            Usuario usuario,
            Estoque estoque,
            TipoMov tipoMov,
            Integer quantidade,
            Integer quantidadePre,
            Integer quantidadePos,
            SetorHosp setor,
            String motivo) {

        return criarHistorico(
                usuario,
                estoque,
                tipoMov,
                quantidade,
                quantidadePre,
                quantidadePos,
                setor,
                motivo,
                null
        );
    }

    private Historico criarHistorico(
            Usuario usuario,
            Estoque estoque,
            TipoMov tipoMov,
            Integer quantidade,
            Integer quantidadePre,
            Integer quantidadePos,
            SetorHosp setor,
            String motivo,
            String identificacaoProfissional) {

        Historico historico = new Historico();

        historico.setTipoMov(tipoMov);
        historico.setEstoque(estoque);
        historico.setQuantidade(quantidade);
        historico.setDataMov(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setSetorHosp(setor);

        String identificacao = (identificacaoProfissional != null && !identificacaoProfissional.isBlank())
                ? identificacaoProfissional
                : (usuario.getCargo() != null ? usuario.getCargo().name() : "USUARIO");

        historico.setNomeFunc(identificacao);
        historico.setMotivoObs(motivo);
        historico.setQuantidadePre(quantidadePre);
        historico.setQuantidadePos(quantidadePos);

        return historico;
    }
}