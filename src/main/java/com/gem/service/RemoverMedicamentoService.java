
package com.gem.service;

import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.Medicamento;
import com.gem.model.SetorHosp;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import com.gem.reps.MedicamentoRepository;
import com.gem.security.SessaoUsuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoverMedicamentoService {

    private final EstoqueRepository estoqueRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final HistoricoRepository historicoRepository;
    private final SessaoUsuario sessaoUsuario;

    public RemoverMedicamentoService(
            EstoqueRepository estoqueRepository,
            MedicamentoRepository medicamentoRepository,
            HistoricoRepository historicoRepository,
            SessaoUsuario sessaoUsuario) {

        this.estoqueRepository = estoqueRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.historicoRepository = historicoRepository;
        this.sessaoUsuario = sessaoUsuario;
    }

    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentosDisponiveis() {

        List<Medicamento> disponiveis = new ArrayList<>();

        for (Medicamento medicamento : medicamentoRepository.findAll()) {

            if (!Boolean.TRUE.equals(medicamento.getAtivo())) {
                continue;
            }

            if (obterQuantidadeDisponivel(medicamento) > 0) {
                disponiveis.add(medicamento);
            }
        }

        return disponiveis;
    }

    @Transactional(readOnly = true)
    public int obterQuantidadeDisponivel(Medicamento medicamento) {

        validarMedicamento(medicamento);

        return estoqueRepository
                .findByMedicamentoIdOrderByIdAsc(medicamento.getId())
                .stream()
                .filter(estoque -> estoque.getQuantidade() != null)
                .mapToInt(Estoque::getQuantidade)
                .filter(quantidade -> quantidade > 0)
                .sum();
    }

    @Transactional
    public void removerEstoque(
            Medicamento medicamento,
            Integer quantidadeRetirada,
            String motivo) {

        validarMedicamento(medicamento);

        if (quantidadeRetirada == null || quantidadeRetirada <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser um inteiro positivo."
            );
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo da remoção é obrigatório."
            );
        }

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        if (usuario == null) {
            throw new IllegalStateException(
                    "Não existe um usuário autenticado."
            );
        }

        Medicamento medicamentoPersistido =
                medicamentoRepository.findById(medicamento.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Medicamento não encontrado."
                        ));

        if (!Boolean.TRUE.equals(medicamentoPersistido.getAtivo())) {
            throw new IllegalStateException(
                    "Não é possível remover estoque de um medicamento inativo."
            );
        }

        List<Estoque> estoques =
                estoqueRepository.findByMedicamentoIdOrderByIdAsc(
                        medicamentoPersistido.getId()
                );

        int quantidadeDisponivel = estoques.stream()
                .filter(estoque -> estoque.getQuantidade() != null)
                .mapToInt(Estoque::getQuantidade)
                .filter(quantidade -> quantidade > 0)
                .sum();

        if (quantidadeDisponivel < quantidadeRetirada) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: "
                            + quantidadeDisponivel + "."
            );
        }

        int restante = quantidadeRetirada;

        for (Estoque estoque : estoques) {

            if (restante == 0) {
                break;
            }

            Integer quantidadeAtual = estoque.getQuantidade();

            if (quantidadeAtual == null || quantidadeAtual <= 0) {
                continue;
            }

            int retirada = Math.min(quantidadeAtual, restante);
            int posterior = quantidadeAtual - retirada;

            estoque.setQuantidade(posterior);
            estoqueRepository.save(estoque);

            Historico historico = new Historico();
            historico.setTipoMov(TipoMov.SAIDA);
            historico.setEstoque(estoque);
            historico.setQuantidade(retirada);
            historico.setDataMov(LocalDateTime.now());
            historico.setUsuario(usuario);
            historico.setSetorHosp(SetorHosp.SATELITE);
            historico.setNomeFunc("Funcionário CAF");
            historico.setMotivoObs(motivo.trim());
            historico.setQuantidadePre(quantidadeAtual);
            historico.setQuantidadePos(posterior);

            historicoRepository.save(historico);

            restante -= retirada;
        }

        boolean existeSaldo = estoqueRepository
                .findByMedicamentoIdOrderByIdAsc(
                        medicamentoPersistido.getId()
                )
                .stream()
                .anyMatch(estoque ->
                        estoque.getQuantidade() != null
                        && estoque.getQuantidade() > 0
                );

        if (!existeSaldo) {
            medicamentoPersistido.setAtivo(false);
            medicamentoRepository.save(medicamentoPersistido);
        }
    }

    private void validarMedicamento(Medicamento medicamento) {

        if (medicamento == null || medicamento.getId() == null) {
            throw new IllegalArgumentException(
                    "Selecione um medicamento válido."
            );
        }
    }
}
