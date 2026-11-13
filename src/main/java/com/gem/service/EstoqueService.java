package com.gem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gem.model.Estoque;
import com.gem.model.Permissao;
import com.gem.model.TipoMed;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

        if (estoqueId == null || !estoqueRepository.existsById(estoqueId)) {
            throw new IllegalArgumentException(
                    "Registro de estoque não encontrado."
            );
        }

        estoqueRepository.deleteById(estoqueId);
    }

    // ------------------------------------------------------------------
    // US-18 (busca por lote) e US-19 (filtro por classe terapêutica)
    // ------------------------------------------------------------------

    /**
     * Um item só aparece na listagem se atender a TODOS os critérios ativos.
     *
     * @param termo   texto da barra de busca (princípio ativo, dose e/ou lote);
     *                vários termos separados por espaço devem ser atendidos juntos
     * @param classes classes terapêuticas selecionadas; vazio ou null = todas
     */
    public boolean corresponde(Estoque estoque, String termo, Set<TipoMed> classes) {
        if (estoque == null) {
            return false;
        }

        return correspondeClasse(estoque, classes) && correspondeBusca(estoque, termo);
    }

    public List<Estoque> filtrar(List<Estoque> itens, String termo, Set<TipoMed> classes) {
        return itens.stream()
                .filter(estoque -> corresponde(estoque, termo, classes))
                .toList();
    }

    private boolean correspondeClasse(Estoque estoque, Set<TipoMed> classes) {
        if (classes == null || classes.isEmpty()) {
            return true;
        }

        TipoMed tipo = estoque.getTipoMed();
        return tipo != null && classes.contains(tipo);
    }

    private boolean correspondeBusca(Estoque estoque, String termo) {
        if (termo == null || termo.isBlank()) {
            return true;
        }

        String alvo = normalizar(
                valor(estoque.getPrincipioAtivo()) + " "
                + valor(estoque.getDose()) + " "
                + valor(estoque.getLote())
        );

        for (String token : normalizar(termo).split("\\s+")) {
            if (!token.isEmpty() && !alvo.contains(token)) {
                return false;
            }
        }

        return true;
    }

    private String valor(String texto) {
        return texto != null ? texto : "";
    }

    /** Ignora maiúsculas/minúsculas e acentos ("antibiótico" = "ANTIBIOTICO"). */
    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
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