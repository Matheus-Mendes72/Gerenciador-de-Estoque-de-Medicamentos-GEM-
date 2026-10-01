package com.gem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gem.model.Estoque;
import com.gem.model.Permissao;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final PermissaoService permissaoService;

    public EstoqueService(
            EstoqueRepository estoqueRepository,
            PermissaoService permissaoService) {

        this.estoqueRepository = estoqueRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional
    public Estoque cadastrar(
            Usuario usuario,
            Estoque estoque) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.CADASTRAR_MEDICAMENTO
        );

        validarEstoque(estoque);

        return estoqueRepository.save(estoque);
    }

    @Transactional
    public Estoque atualizar(
            Usuario usuario,
            Estoque estoque) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.ATUALIZAR_MEDICAMENTO
        );

        if (estoque.getId() == null) {
            throw new IllegalArgumentException(
                    "Medicamento não possui ID."
            );
        }

        if (!estoqueRepository.existsById(estoque.getId())) {
            throw new IllegalArgumentException(
                    "Medicamento não encontrado."
            );
        }

        validarEstoque(estoque);

        return estoqueRepository.save(estoque);
    }

    @Transactional
    public void remover(
            Usuario usuario,
            Integer estoqueId) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REMOVER_MEDICAMENTO
        );

        if (!estoqueRepository.existsById(estoqueId)) {
            throw new IllegalArgumentException(
                    "Medicamento não encontrado."
            );
        }

        estoqueRepository.deleteById(estoqueId);
    }

    private void validarEstoque(Estoque estoque) {

        if (estoque == null) {
            throw new IllegalArgumentException(
                    "Medicamento não informado."
            );
        }

        if (estoque.getPrincipioAtivo() == null ||
            estoque.getPrincipioAtivo().isBlank()) {

            throw new IllegalArgumentException(
                    "Princípio ativo é obrigatório."
            );
        }

        if (estoque.getDose() == null ||
            estoque.getDose().isBlank()) {

            throw new IllegalArgumentException(
                    "Dose é obrigatória."
            );
        }

        if (estoque.getTipoMed() == null) {
            throw new IllegalArgumentException(
                    "Tipo do medicamento é obrigatório."
            );
        }

        if (estoque.getQuantidade() == null ||
            estoque.getQuantidade() < 0) {

            throw new IllegalArgumentException(
                    "Quantidade inválida."
            );
        }
    }
}