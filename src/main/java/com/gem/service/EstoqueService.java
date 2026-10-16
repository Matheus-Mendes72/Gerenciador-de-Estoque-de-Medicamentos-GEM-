
package com.gem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
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

        if (estoque == null || estoque.getId() == null) {
            throw new IllegalArgumentException(
                    "Registro de estoque não possui ID."
            );
        }

        if (!estoqueRepository.existsById(estoque.getId())) {
            throw new IllegalArgumentException(
                    "Registro de estoque não encontrado."
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

        if (estoqueId == null ||
            !estoqueRepository.existsById(estoqueId)) {

            throw new IllegalArgumentException(
                    "Registro de estoque não encontrado."
            );
        }

        estoqueRepository.deleteById(estoqueId);
    }

    private void validarEstoque(Estoque estoque) {

        if (estoque == null) {
            throw new IllegalArgumentException(
                    "Registro de estoque não informado."
            );
        }

        Medicamento medicamento = estoque.getMedicamento();

        if (medicamento == null || medicamento.getId() == null) {
            throw new IllegalArgumentException(
                    "Medicamento é obrigatório."
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
