package com.gem.service;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.model.Permissao;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.MedicamentoRepository;
import com.gem.security.SessaoUsuario;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final EstoqueRepository estoqueRepository;
    private final PermissaoService permissaoService;
    private final SessaoUsuario sessaoUsuario;

    public MedicamentoService(
            MedicamentoRepository medicamentoRepository,
            EstoqueRepository estoqueRepository,
            PermissaoService permissaoService,
            SessaoUsuario sessaoUsuario) {

        this.medicamentoRepository = medicamentoRepository;
        this.estoqueRepository = estoqueRepository;
        this.permissaoService = permissaoService;
        this.sessaoUsuario = sessaoUsuario;
    }

    @Transactional
    public Medicamento cadastrarComEstoque(
            Usuario usuario,
            Medicamento medicamento,
            Estoque estoque) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.CADASTRAR_MEDICAMENTO
        );

        validarDadosObrigatorios(medicamento, estoque);

        medicamento.setAtivo(true);

        // 1. Salva o medicamento no catálogo
        Medicamento medicamentoSalvo =
                medicamentoRepository.save(medicamento);

        // 2. Associa o lote e medicamento ao estoque
        estoque.setMedicamento(medicamentoSalvo);

        // 3. Salva o lote no estoque
        estoqueRepository.save(estoque);

        return medicamentoSalvo;
    }

    @Transactional
    public Medicamento cadastrarComEstoque(
            Medicamento medicamento,
            Estoque estoque) {

        Usuario usuarioAtual = sessaoUsuario.getUsuarioAtual();

        return cadastrarComEstoque(usuarioAtual, medicamento, estoque);
    }

    private void validarDadosObrigatorios(Medicamento medicamento, Estoque estoque) {
        if (medicamento == null) {
            throw new IllegalArgumentException("Medicamento não informado.");
        }

        if (medicamento.getPrincipioAtivo() == null || medicamento.getPrincipioAtivo().isBlank()) {
            throw new IllegalArgumentException("Princípio ativo é obrigatório.");
        }

        if (medicamento.getDose() == null || medicamento.getDose().isBlank()) {
            throw new IllegalArgumentException("Dose é obrigatória.");
        }

        if (medicamento.getTipoMed() == null) {
            throw new IllegalArgumentException("Classe terapêutica (tipo) é obrigatória.");
        }

        if (medicamento.getLaboratorio() == null || medicamento.getLaboratorio().isBlank()) {
            throw new IllegalArgumentException("Laboratório é obrigatório.");
        }

        if (estoque == null) {
            throw new IllegalArgumentException("Dados de estoque inicial não informados.");
        }

        if (estoque.getQuantidade() == null || estoque.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade inicial deve ser maior que zero.");
        }

        if (estoque.getLote() == null || estoque.getLote().isBlank()) {
            throw new IllegalArgumentException("Lote inicial é obrigatório.");
        }
    }
}
