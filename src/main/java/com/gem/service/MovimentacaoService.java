package com.gem.service;

import com.gem.model.CargoProfissional;
import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.SetorHosp;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MovimentacaoService {

    private final EstoqueRepository estoqueRepository;
    private final HistoricoRepository historicoRepository;

    public MovimentacaoService(
            EstoqueRepository estoqueRepository,
            HistoricoRepository historicoRepository) {
        this.estoqueRepository = estoqueRepository;
        this.historicoRepository = historicoRepository;
    }

    @Transactional
    public Historico registrarSaida(
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            Usuario usuario) {

        validarFarmaceutico(usuario);

        Estoque estoque = buscarEstoque(estoqueId);

        validarQuantidade(quantidade);

        if (estoque.getQuantidade() < quantidade) {
            throw new IllegalArgumentException(
                    "Quantidade insuficiente no estoque.");
        }

        int quantidadeAnterior = estoque.getQuantidade();
        int quantidadePosterior = quantidadeAnterior - quantidade;

        estoque.setQuantidade(quantidadePosterior);

        estoqueRepository.save(estoque);

        Historico historico = criarHistorico(
                TipoMov.SAIDA,
                estoque,
                quantidade,
                usuario,
                setor,
                motivo,
                quantidadeAnterior,
                quantidadePosterior);

        return historicoRepository.save(historico);
    }

    @Transactional
    public Historico registrarRetorno(
            Integer estoqueId,
            Integer quantidade,
            SetorHosp setor,
            String motivo,
            Usuario usuario) {

        validarFarmaceutico(usuario);

        Estoque estoque = buscarEstoque(estoqueId);

        validarQuantidade(quantidade);

        int quantidadeAnterior = estoque.getQuantidade();
        int quantidadePosterior = quantidadeAnterior + quantidade;

        estoque.setQuantidade(quantidadePosterior);

        estoqueRepository.save(estoque);

        Historico historico = criarHistorico(
                TipoMov.RETORNO,
                estoque,
                quantidade,
                usuario,
                setor,
                motivo,
                quantidadeAnterior,
                quantidadePosterior);

        return historicoRepository.save(historico);
    }

    private void validarFarmaceutico(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Usuário não autenticado.");
        }

        if (usuario.getCargo() != CargoProfissional.FUNCIONARIO_CAF) {
            throw new IllegalArgumentException(
                    "Usuário sem permissão para realizar esta operação.");
        }
    }

    private Estoque buscarEstoque(Integer estoqueId) {

        return estoqueRepository.findById(estoqueId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Medicamento não encontrado."));
    }

    private void validarQuantidade(Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero.");
        }
    }

    private Historico criarHistorico(
            TipoMov tipoMov,
            Estoque estoque,
            Integer quantidade,
            Usuario usuario,
            SetorHosp setor,
            String motivo,
            Integer quantidadeAnterior,
            Integer quantidadePosterior) {

        Historico historico = new Historico();

        historico.setTipoMov(tipoMov);
        historico.setEstoque(estoque);
        historico.setQuantidade(quantidade);
        historico.setDataMov(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setSetorHosp(setor);
        historico.setNomeFunc(usuario.getCargo().name());
        historico.setMotivoObs(motivo);
        historico.setQuantidadePre(quantidadeAnterior);
        historico.setQuantidadePos(quantidadePosterior);

        return historico;
    }
}