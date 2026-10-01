package com.gem.service;

import com.gem.model.*;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @InjectMocks
    private MovimentacaoService movimentacaoService;

    private Usuario farmaceutico;
    private Usuario coordenador;
    private Estoque estoque;

    @BeforeEach
    void setUp() {
        // Inicializa os perfis de usuário conforme a regra do sistema
        farmaceutico = new Usuario(CargoProfissional.FUNCIONARIO_CAF, "hashHash");
        coordenador = new Usuario(CargoProfissional.COORDENADOR, "hashHash");

        // Inicializa um item de estoque fictício
        estoque = new Estoque();
        estoque.setPrincipioAtivo("Dipirona");
        estoque.setDose("500mg");
        estoque.setTipoMed(TipoMed.CONTROLADO);
        estoque.setQuantidade(50);
    }

    @Test
    void deveValidarPermissaoERegistrarMovimentacaoDoFarmaceuticoCT10() {
        // Simula o comportamento do repositório
        when(estoqueRepository.findById(1)).thenReturn(Optional.of(estoque));
        when(historicoRepository.save(any(Historico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executa a saída de medicamentos como Farmacêutico (CT-09 / CT-10)
        Historico historico = movimentacaoService.registrarSaida(
                1, 5, SetorHosp.EMERGENCIA, "Uso na emergência", farmaceutico
        );

        // Validações do CT-10 (Registro do Farmacêutico: identificação, data e hora)
        assertNotNull(historico, "O histórico da movimentação não deve ser nulo.");
        assertEquals(farmaceutico, historico.getUsuario(), "Deve registrar a identificação do usuário profissional.");
        assertEquals("FUNCIONARIO_CAF", historico.getNomeFunc(), "Deve registrar o nome/cargo do funcionário.");
        assertNotNull(historico.getDataMov(), "Deve registrar a data da movimentação.");
        assertEquals(45, estoque.getQuantidade(), "O estoque deve ser atualizado corretamente (50 - 5 = 45).");
        
        verify(historicoRepository, times(1)).save(any(Historico.class));
    }

    @Test
    void deveBloquearAcessoDeOutrosCargosCT09() {
        // Tenta realizar uma movimentação restrita usando um cargo sem permissão (CT-09)
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> movimentacaoService.registrarSaida(1, 5, SetorHosp.EMERGENCIA, "Tentativa indevida", coordenador)
        );

        // Valida que a permissão foi respeitada e o acesso foi barrado
        assertEquals("Usuário sem permissão para realizar esta operação.", excecao.getMessage());
        verify(estoqueRepository, never()).findById(anyInt());
        verify(historicoRepository, never()).save(any(Historico.class));
    }
}