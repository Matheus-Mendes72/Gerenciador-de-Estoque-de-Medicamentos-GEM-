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

    @Transactional
    public Historico registrarRetorno(
            Usuario usuario,
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REGISTRAR_RETORNO
        );

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
                motivo
        );

        return historicoRepository.save(historico);
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

        Historico historico = new Historico();

        historico.setTipoMov(tipoMov);
        historico.setEstoque(estoque);
        historico.setQuantidade(quantidade);
        historico.setDataMov(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setSetorHosp(setor);

        /*
         * O ID do usuário é a identificação real do profissional.
         * nomeFunc deve seguir o que vocês definiram no modelo.
         */
        historico.setNomeFunc(
                usuario.getCargo().name()
        );

        historico.setMotivoObs(motivo);
        historico.setQuantidadePre(quantidadePre);
        historico.setQuantidadePos(quantidadePos);

        return historico;
    }
}