package com.gem;

import com.gem.model.Estoque;
import com.gem.model.TipoMed;
import com.gem.reps.EstoqueRepository;
import com.gem.service.EstoqueService;
import com.gem.service.PermissaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class EstoqueServiceTest {

    private EstoqueService service;

    private Estoque dipirona;
    private Estoque amoxicilina;
    private Estoque clonazepam;
    private Estoque semTipo;

    private List<Estoque> todos;

    @BeforeEach
    void setUp() {
        service = new EstoqueService(
                mock(EstoqueRepository.class),
                mock(PermissaoService.class)
        );

        dipirona = novo("Dipirona", "500mg", TipoMed.ANTIBIOTICO, "LOT100");
        amoxicilina = novo("Amoxicilina", "500mg", TipoMed.ANTIBIOTICO, "AMX200");
        clonazepam = novo("Clonazepam", "2mg", TipoMed.CONTROLADO, "CLN300");
        semTipo = novo("Soro", "500ml", null, "SRO400");

        todos = List.of(dipirona, amoxicilina, clonazepam, semTipo);
    }

    private Estoque novo(String principioAtivo, String dose, TipoMed tipo, String lote) {
        Estoque estoque = new Estoque();
        estoque.setPrincipioAtivo(principioAtivo);
        estoque.setDose(dose);
        estoque.setTipoMed(tipo);
        estoque.setLote(lote);
        estoque.setQuantidade(10);
        return estoque;
    }

    // ------------------------------------------------------------------
    // US-19: filtro por classe terapêutica
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US-19: Sem filtros, todos os medicamentos são exibidos")
    void deveRetornarTodos_QuandoSemFiltros() {
        assertEquals(4, service.filtrar(todos, "", EnumSet.noneOf(TipoMed.class)).size());
        assertEquals(4, service.filtrar(todos, null, null).size());
    }

    @Test
    @DisplayName("US-19: Filtro por uma classe exibe apenas medicamentos dessa classe")
    void deveFiltrarPorUmaClasse() {
        List<Estoque> resultado = service.filtrar(todos, "", EnumSet.of(TipoMed.CONTROLADO));

        assertEquals(List.of(clonazepam), resultado);
    }

    @Test
    @DisplayName("US-19: Seleção de múltiplas classes exibe medicamentos de qualquer uma delas")
    void deveFiltrarPorMultiplasClasses() {
        List<Estoque> resultado = service.filtrar(
                todos, "", EnumSet.of(TipoMed.ANTIBIOTICO, TipoMed.CONTROLADO));

        assertEquals(3, resultado.size());
        assertFalse(resultado.contains(semTipo), "Item sem classe não deve aparecer com filtro de classe ativo");
    }

    // ------------------------------------------------------------------
    // US-18: busca por princípio ativo e por lote
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US-18: Deve buscar pelo número do lote")
    void deveBuscarPorLote() {
        assertEquals(List.of(clonazepam), service.filtrar(todos, "CLN300", Set.of()));
    }

    @Test
    @DisplayName("US-18: Busca por lote parcial e sem diferenciar maiúsculas")
    void deveBuscarPorLoteParcialIgnorandoCaixa() {
        assertEquals(List.of(amoxicilina), service.filtrar(todos, "amx", Set.of()));
    }

    @Test
    @DisplayName("US-18: Deve buscar por princípio ativo ignorando acentos e caixa")
    void deveBuscarPorPrincipioAtivo() {
        assertEquals(List.of(dipirona), service.filtrar(todos, "DIPIRONA", Set.of()));
        assertEquals(List.of(dipirona), service.filtrar(todos, "dipirôna", Set.of()));
    }

    @Test
    @DisplayName("US-18: Quando nada corresponde ao lote, a lista retorna vazia")
    void deveRetornarVazio_QuandoLoteNaoExiste() {
        assertTrue(service.filtrar(todos, "LOTE-INEXISTENTE", Set.of()).isEmpty());
    }

    // ------------------------------------------------------------------
    // Combinação de critérios (AND)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US-18/19: Busca combinada com classe exige TODOS os critérios")
    void deveExigirTodosOsCriterios_QuandoBuscaECombinadaComClasse() {
        List<Estoque> resultado = service.filtrar(todos, "500", EnumSet.of(TipoMed.ANTIBIOTICO));

        assertEquals(2, resultado.size());
        assertTrue(resultado.containsAll(List.of(dipirona, amoxicilina)));
    }

    @Test
    @DisplayName("US-18/19: Busca que existe em outra classe não retorna resultado")
    void deveRetornarVazio_QuandoBuscaNaoPertenceAClasseSelecionada() {
        assertTrue(service.filtrar(todos, "CLN300", EnumSet.of(TipoMed.ANTIBIOTICO)).isEmpty());
    }

    @Test
    @DisplayName("US-18: Princípio ativo e lote podem ser usados juntos na busca")
    void deveBuscarPorPrincipioAtivoELoteSimultaneamente() {
        assertEquals(List.of(dipirona), service.filtrar(todos, "dipirona LOT100", Set.of()));
        assertTrue(service.filtrar(todos, "dipirona AMX200", Set.of()).isEmpty(),
                "Todos os termos precisam ser atendidos pelo mesmo item");
    }

    @Test
    @DisplayName("Item nulo nunca corresponde")
    void naoDeveCorresponder_QuandoEstoqueNulo() {
        assertFalse(service.corresponde(null, "", Set.of()));
    }
}