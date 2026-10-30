package com.gem;

import com.gem.model.CargoProfissional;
import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.Medicamento;
import com.gem.model.Permissao;
import com.gem.model.TipoMed;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import com.gem.reps.MedicamentoRepository;
import com.gem.security.SessaoUsuario;
import com.gem.service.PermissaoService;
import com.gem.service.RemoverMedicamentoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoverMedicamentoServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private SessaoUsuario sessaoUsuario;

    @Mock
    private PermissaoService permissaoService;

    @InjectMocks
    private RemoverMedicamentoService removerMedicamentoService;

    private Usuario coordenador;
    private Usuario tecnico;
    private Medicamento medicamentoAtivo;

    @BeforeEach
    void setUp() {
        coordenador = new Usuario(CargoProfissional.COORDENADOR, "senhaHash");
        tecnico = new Usuario(CargoProfissional.FUNCIONARIO_SAT, "senhaHash");

        medicamentoAtivo = new Medicamento();
        medicamentoAtivo.setId(1); // requer Medicamento#setId (ver observação)
        medicamentoAtivo.setPrincipioAtivo("Dipirona");
        medicamentoAtivo.setDose("500mg");
        medicamentoAtivo.setTipoMed(TipoMed.ANTIBIOTICO);
        medicamentoAtivo.setLaboratorio("EMS");
        medicamentoAtivo.setAtivo(true);
    }

    private Estoque novoEstoque(Integer quantidade) {
        Estoque estoque = new Estoque();
        estoque.setMedicamento(medicamentoAtivo);
        estoque.setQuantidade(quantidade);
        return estoque;
    }

    // ------------------------------------------------------------------
    // Remoção do catálogo (soft delete)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("US-09: Coordenador deve remover medicamento do catálogo com sucesso (soft delete)")
    void deveRemoverMedicamentoDoCatalogoComSucesso_QuandoCoordenador() {
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque estoque = novoEstoque(50);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        Medicamento resultado = removerMedicamentoService.removerDoCatalogo(coordenador, 1, "Descontinuação do lote");

        assertNotNull(resultado);
        assertFalse(resultado.getAtivo(), "Medicamento deve ser inativado (removido do catálogo)");
        assertEquals(0, estoque.getQuantidade(), "Estoque ativo deve ser zerado ao remover do catálogo");

        verify(permissaoService, times(1)).exigirPermissao(coordenador, Permissao.REMOVER_MEDICAMENTO);
        verify(medicamentoRepository, times(1)).save(medicamentoAtivo);
        verify(historicoRepository, times(1)).save(any(Historico.class));
    }

    @Test
    @DisplayName("US-09: Remoção do catálogo sem saldo inativa o medicamento e não gera histórico")
    void deveInativarMedicamentoSemSaldoNoCatalogo() {
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(novoEstoque(0)));

        Medicamento resultado = removerMedicamentoService.removerDoCatalogo(coordenador, 1, "Sem uso");

        assertFalse(resultado.getAtivo());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-09: Deve rejeitar motivo que não cabe na coluna ao remover do catálogo, sem alterar nada")
    void deveFalhar_QuandoMotivoDoCatalogoMuitoLongo() {
        String motivoLongo = "x".repeat(90);

        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerDoCatalogo(coordenador, 1, motivoLongo)
        );

        verify(medicamentoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-09: Deve lançar exceção quando usuário não tiver permissão para remover medicamento")
    void deveLancarExcecao_QuandoUsuarioSemPermissao() {
        doThrow(new IllegalArgumentException("Usuário sem permissão"))
                .when(permissaoService).exigirPermissao(tecnico, Permissao.REMOVER_MEDICAMENTO);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerDoCatalogo(tecnico, 1, "Tentativa não autorizada")
        );

        assertEquals("Usuário sem permissão", ex.getMessage());
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-09: Deve lançar exceção quando medicamento não for encontrado")
    void deveLancarExcecao_QuandoMedicamentoNaoExistir() {
        when(medicamentoRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerDoCatalogo(coordenador, 999, "Motivo")
        );
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-09: Listar medicamentos disponíveis deve retornar apenas os que estão ativos no catálogo")
    void deveListarApenasMedicamentosAtivos() {
        when(medicamentoRepository.findByAtivoTrue()).thenReturn(List.of(medicamentoAtivo));

        List<Medicamento> disponiveis = removerMedicamentoService.listarMedicamentosDisponiveis();

        assertEquals(1, disponiveis.size());
        assertEquals("Dipirona", disponiveis.get(0).getPrincipioAtivo());
        verify(medicamentoRepository, times(1)).findByAtivoTrue();
    }

    // ------------------------------------------------------------------
    // Saldo disponível
    // ------------------------------------------------------------------

    @Test
    @DisplayName("BUG: Saldo disponível soma todos os lotes e ignora valores nulos ou negativos")
    void deveSomarSaldoDeTodosOsLotes() {
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(
                Arrays.asList(novoEstoque(10), novoEstoque(null), novoEstoque(-5), novoEstoque(5))
        );

        assertEquals(15, removerMedicamentoService.obterQuantidadeDisponivel(medicamentoAtivo));
    }

    // ------------------------------------------------------------------
    // Remoção de quantidade (bug de remoção no Estoque)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("BUG: Deve remover quantidade parcial exata mantendo o medicamento ativo se ainda houver saldo")
    void deveRemoverQuantidadeParcialExata() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque estoque = novoEstoque(50);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 20, "Descarte de 20 unidades danificadas");

        assertEquals(30, estoque.getQuantidade(), "Estoque deve ser reduzido na quantidade exata de 20 unidades");
        assertTrue(medicamentoAtivo.getAtivo(), "Medicamento deve continuar ativo pois ainda resta saldo");
        verify(estoqueRepository, times(1)).save(estoque);
        verify(historicoRepository, times(1)).save(any(Historico.class));
        verify(medicamentoRepository, never()).save(any(Medicamento.class));
    }

    @Test
    @DisplayName("BUG: Deve remover quantidade total e inativar medicamento quando estoque zerar")
    void deveRemoverQuantidadeTotalEInativarMedicamento() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque estoque = novoEstoque(50);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 50, "Descarte do lote todo");

        assertEquals(0, estoque.getQuantidade(), "Estoque deve ser zerado ao remover 50 unidades");
        assertFalse(medicamentoAtivo.getAtivo(), "Medicamento deve ser inativado quando estoque chegar a zero");
        verify(estoqueRepository, times(1)).save(estoque);
        verify(medicamentoRepository, times(1)).save(medicamentoAtivo);
        verify(historicoRepository, times(1)).save(any(Historico.class));
    }

    @Test
    @DisplayName("BUG: Estoque com quantidade igual a 1 - remover 1 zera e inativa")
    void deveRemoverQuandoQuantidadeEmEstoqueIgualAUm() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque estoque = novoEstoque(1);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "Unidade vencida");

        assertEquals(0, estoque.getQuantidade());
        assertFalse(medicamentoAtivo.getAtivo());
        verify(historicoRepository, times(1)).save(any(Historico.class));
    }

    @Test
    @DisplayName("BUG: Estoque maior que 1 - remover 1 unidade mantém o restante e o medicamento ativo")
    void deveRemoverUmaUnidadeQuandoEstoqueMaiorQueUm() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque estoque = novoEstoque(10);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "Unidade danificada");

        assertEquals(9, estoque.getQuantidade());
        assertTrue(medicamentoAtivo.getAtivo());
        verify(medicamentoRepository, never()).save(any(Medicamento.class));
    }

    @Test
    @DisplayName("BUG: Remoção maior que um lote deve consumir os lotes em ordem e registrar um histórico por lote")
    void deveRemoverAtravesDeMultiplosLotes() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque lote1 = novoEstoque(30);
        Estoque lote2 = novoEstoque(40);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(lote1, lote2));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 50, "Recolhimento parcial");

        assertEquals(0, lote1.getQuantidade());
        assertEquals(20, lote2.getQuantidade());
        assertTrue(medicamentoAtivo.getAtivo(), "Ainda resta saldo no segundo lote");
        verify(estoqueRepository, times(2)).save(any(Estoque.class));
        verify(historicoRepository, times(2)).save(any(Historico.class));
        verify(medicamentoRepository, never()).save(any(Medicamento.class));
    }

    @Test
    @DisplayName("BUG: Deve falhar ao tentar remover quantidade maior que o estoque disponível")
    void deveFalhar_QuandoEstoqueInsuficiente() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        Estoque estoque = novoEstoque(15);
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(estoque));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 20, "Tentativa de remover mais do que tem")
        );

        assertTrue(ex.getMessage().contains("Estoque insuficiente"));
        assertEquals(15, estoque.getQuantidade(), "Estoque não pode ser alterado em caso de falha");
        verify(estoqueRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Medicamento ativo sem saldo deve orientar a remoção pelo catálogo")
    void deveFalhar_QuandoMedicamentoSemSaldo() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(novoEstoque(0)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "Qualquer motivo")
        );

        assertTrue(ex.getMessage().contains("sem saldo"));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Deve rejeitar quantidade zero, negativa ou nula")
    void deveFalhar_QuandoQuantidadeInvalida() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);

        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 0, "Motivo"));
        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, -3, "Motivo"));
        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, null, "Motivo"));

        verify(estoqueRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Deve rejeitar motivo vazio")
    void deveFalhar_QuandoMotivoVazio() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);

        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "   "));

        verify(estoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Deve rejeitar com mensagem clara motivo maior que 100 caracteres (coluna motivo_obs)")
    void deveFalhar_QuandoMotivoMaiorQueLimiteDaColuna() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        String motivoLongo = "x".repeat(101);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, motivoLongo)
        );

        assertTrue(ex.getMessage().contains("100"));
        verify(estoqueRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Deve aceitar motivo com exatamente 100 caracteres")
    void deveAceitarMotivoComExatamenteCemCaracteres() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));
        when(estoqueRepository.findByMedicamentoIdOrderByIdAsc(1)).thenReturn(List.of(novoEstoque(10)));

        removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "x".repeat(100));

        verify(historicoRepository, times(1)).save(any(Historico.class));
    }

    @Test
    @DisplayName("BUG: Usuário sem permissão não pode remover estoque")
    void deveBloquearRemocaoDeEstoque_QuandoSemPermissao() {
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(tecnico);
        doThrow(new IllegalArgumentException("Usuário sem permissão"))
                .when(permissaoService).exigirPermissao(tecnico, Permissao.REMOVER_MEDICAMENTO);

        assertThrows(IllegalArgumentException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "Motivo"));

        verify(estoqueRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("BUG: Não é possível remover estoque de medicamento já inativo")
    void deveFalhar_QuandoMedicamentoInativo() {
        medicamentoAtivo.setAtivo(false);
        when(sessaoUsuario.getUsuarioAtual()).thenReturn(coordenador);
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));

        assertThrows(IllegalStateException.class, () ->
                removerMedicamentoService.removerEstoque(medicamentoAtivo, 1, "Motivo"));

        verify(estoqueRepository, never()).save(any());
    }
}