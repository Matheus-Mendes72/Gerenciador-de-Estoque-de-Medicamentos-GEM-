package com.gem;

import com.gem.model.CargoProfissional;
import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.Permissao;
import com.gem.model.SetorHosp;
import com.gem.model.TipoMed;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import com.gem.service.MovimentacaoService;
import com.gem.service.PermissaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private HistoricoRepository historicoRepository;

    @Mock
    private PermissaoService permissaoService;

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Usuario farmaceutico;
    private Usuario coordenador;
    private Estoque estoque;

    @BeforeEach
    void setUp() {
        farmaceutico = new Usuario(
                CargoProfissional.FUNCIONARIO_CAF,
                "hashHash"
        );

        coordenador = new Usuario(
                CargoProfissional.COORDENADOR,
                "hashHash"
        );

        estoque = new Estoque();
        estoque.setPrincipioAtivo("Dipirona");
        estoque.setDose("500mg");
        estoque.setTipoMed(TipoMed.CONTROLADO);
        estoque.setQuantidade(50);
    }

    @Test
    void deveValidarPermissaoERegistrarMovimentacaoDoFarmaceuticoCT10() {

        when(estoqueRepository.findById(1))
                .thenReturn(Optional.of(estoque));

        when(historicoRepository.save(any(Historico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(permissaoService)
                .exigirPermissao(
                        farmaceutico,
                        Permissao.REGISTRAR_SAIDA
                );

        Historico historico = movimentacaoService.registrarSaida(
                farmaceutico,
                1,
                5,
                SetorHosp.EMERGENCIA,
                "Uso na emergência"
        );

        assertNotNull(historico);
        assertEquals(farmaceutico, historico.getUsuario());
        assertEquals("FUNCIONARIO_CAF", historico.getNomeFunc());
        assertNotNull(historico.getDataMov());
        assertEquals(45, estoque.getQuantidade());

        verify(historicoRepository, times(1))
                .save(any(Historico.class));
    }

    @Test
    void devePermitirSaidaParaCoordenadorUS23() {

        when(estoqueRepository.findById(1))
                .thenReturn(Optional.of(estoque));

        when(historicoRepository.save(any(Historico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(permissaoService)
                .exigirPermissao(
                        coordenador,
                        Permissao.REGISTRAR_SAIDA
                );

        Historico historico = movimentacaoService.registrarSaida(
                coordenador,
                1,
                5,
                SetorHosp.EMERGENCIA,
                "Uso na emergência"
        );

        assertNotNull(historico);
        assertEquals(coordenador, historico.getUsuario());
        assertEquals("COORDENADOR", historico.getNomeFunc());
        assertNotNull(historico.getDataMov());
        assertEquals(45, estoque.getQuantidade());

        verify(historicoRepository, times(1))
                .save(any(Historico.class));
    }

    @Test
    void deveRegistrarSaidaDoTecnicoUS04() {

        Usuario tecnico = new Usuario(
                CargoProfissional.FUNCIONARIO_SAT,
                "hashHash"
        );

        when(estoqueRepository.findById(1))
                .thenReturn(Optional.of(estoque));

        when(historicoRepository.save(any(Historico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(permissaoService)
                .exigirPermissao(
                        tecnico,
                        Permissao.REGISTRAR_SAIDA
                );

        Historico historico = movimentacaoService.registrarSaida(
                tecnico,
                1,
                5,
                SetorHosp.EMERGENCIA,
                "Medicamento utilizado durante o plantão"
        );

        assertNotNull(historico);
        assertEquals(tecnico, historico.getUsuario());
        assertEquals("FUNCIONARIO_SAT", historico.getNomeFunc());
        assertNotNull(historico.getDataMov());
        assertEquals(45, estoque.getQuantidade());

        verify(historicoRepository, times(1))
                .save(any(Historico.class));
    }
}