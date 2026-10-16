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
        medicamentoAtivo.setPrincipioAtivo("Dipirona");
        medicamentoAtivo.setDose("500mg");
        medicamentoAtivo.setTipoMed(TipoMed.ANTIBIOTICO);
        medicamentoAtivo.setLaboratorio("EMS");
        medicamentoAtivo.setAtivo(true);
    }

    @Test
    @DisplayName("US-09: Coordenador deve remover medicamento do catálogo com sucesso (soft delete)")
    void deveRemoverMedicamentoDoCatalogoComSucesso_QuandoCoordenador() {
        when(medicamentoRepository.findById(1)).thenReturn(Optional.of(medicamentoAtivo));
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque estoque = new Estoque();
        estoque.setMedicamento(medicamentoAtivo);
        estoque.setQuantidade(50);
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
}
