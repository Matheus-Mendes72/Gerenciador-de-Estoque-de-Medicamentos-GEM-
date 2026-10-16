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

import com.gem.dto.SaidaMedicamentoDTO;

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
            SaidaMedicamentoDTO dto) {

        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não informado ou não autenticado.");
        }

        if (dto == null) {
            throw new IllegalArgumentException("Dados da saída de medicamento não informados.");
        }

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REGISTRAR_SAIDA
        );

        dto.validarCamposObrigatorios();

        Estoque estoque = buscarEstoquePorPrincipioAtivo(dto.getPrincipioAtivo());

        if (estoque.getQuantidade() < dto.getQuantidade()) {
            throw new IllegalArgumentException(
                    "Quantidade disponível insuficiente em estoque. Disponível: " + estoque.getQuantidade()
            );
        }

        int quantidadePre = estoque.getQuantidade();
        int quantidadePos = quantidadePre - dto.getQuantidade();

        estoque.setQuantidade(quantidadePos);
        estoqueRepository.save(estoque);

        Historico historico = new Historico();
        historico.setTipoMov(TipoMov.SAIDA);
        historico.setEstoque(estoque);
        historico.setQuantidade(dto.getQuantidade());
        historico.setDataMov(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setSetorHosp(dto.getSetorDestino());
        historico.setNomeFunc(dto.getResponsavelRetirada());
        historico.setMedicoPrescritor(dto.getMedicoPrescritor());
        historico.setMotivoObs(dto.getMotivo());
        historico.setQuantidadePre(quantidadePre);
        historico.setQuantidadePos(quantidadePos);

        return historicoRepository.save(historico);
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

    private Estoque buscarEstoquePorPrincipioAtivo(String principioAtivo) {

        return estoqueRepository.findFirstByPrincipioAtivoIgnoreCase(principioAtivo)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Medicamento não encontrado com o princípio ativo informado: " + principioAtivo
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
                usuario != null && usuario.getCargo() != null
                        ? usuario.getCargo().name()
                        : "USUARIO"
        );

        historico.setMedicoPrescritor(
                tipoMov == TipoMov.RETORNO
                        ? "NÃO APLICÁVEL (RETORNO)"
                        : "NÃO INFORMADO"
        );

        historico.setMotivoObs(motivo);
        historico.setQuantidadePre(quantidadePre);
        historico.setQuantidadePos(quantidadePos);

        return historico;
    }
}