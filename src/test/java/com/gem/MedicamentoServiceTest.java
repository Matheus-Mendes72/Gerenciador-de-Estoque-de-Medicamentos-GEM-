package com.gem;

import com.gem.model.CargoProfissional;
import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.model.Permissao;
import com.gem.model.TipoMed;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.MedicamentoRepository;
import com.gem.security.SessaoUsuario;
import com.gem.service.MedicamentoService;
import com.gem.service.PermissaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicamentoServiceTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private PermissaoService permissaoService;

    @Mock
    private SessaoUsuario sessaoUsuario;

    @InjectMocks
    private MedicamentoService medicamentoService;

    private Usuario coordenador;
    private Usuario tecnico;
    private Medicamento medicamentoValido;
    private Estoque estoqueValido;

    @BeforeEach
    void setUp() {
        coordenador = new Usuario(CargoProfissional.COORDENADOR, "senhaHash");
        tecnico = new Usuario(CargoProfissional.FUNCIONARIO_SAT, "senhaHash");

        medicamentoValido = new Medicamento();
        medicamentoValido.setPrincipioAtivo("Paracetamol");
        medicamentoValido.setDose("750mg");
        medicamentoValido.setTipoMed(TipoMed.ANTIBIOTICO);
        medicamentoValido.setLaboratorio("Medley");

        estoqueValido = new Estoque();
        estoqueValido.setLote("LOT1234");
        estoqueValido.setQuantidade(100);
    }

    @Test
    @DisplayName("US-08: Coordenador deve conseguir cadastrar medicamento com sucesso quando todos os campos obrigatórios forem fornecidos")
    void deveCadastrarMedicamentoComSucesso_QuandoCoordenadorECamposValidos() {
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(invocation -> {
            Medicamento m = invocation.getArgument(0);
            return m;
        });
        when(estoqueRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Medicamento resultado = medicamentoService.cadastrarComEstoque(coordenador, medicamentoValido, estoqueValido);

        assertNotNull(resultado);
        assertTrue(resultado.getAtivo(), "Medicamento cadastrado deve estar ativo no catálogo");
        assertEquals("Paracetamol", resultado.getPrincipioAtivo());
        assertEquals("Medley", resultado.getLaboratorio());
        assertEquals(medicamentoValido, estoqueValido.getMedicamento(), "Estoque deve estar vinculado ao medicamento salvo");

        verify(permissaoService, times(1)).exigirPermissao(coordenador, Permissao.CADASTRAR_MEDICAMENTO);
        verify(medicamentoRepository, times(1)).save(medicamentoValido);
        verify(estoqueRepository, times(1)).save(estoqueValido);
    }

    @Test
    @DisplayName("US-08: Deve impedir cadastro quando usuário não tiver permissão de coordenador")
    void deveLancarExcecao_QuandoUsuarioNaoTiverPermissao() {
        doThrow(new IllegalArgumentException("Usuário sem permissão"))
                .when(permissaoService).exigirPermissao(tecnico, Permissao.CADASTRAR_MEDICAMENTO);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                medicamentoService.cadastrarComEstoque(tecnico, medicamentoValido, estoqueValido)
        );

        assertEquals("Usuário sem permissão", ex.getMessage());
        verify(medicamentoRepository, never()).save(any());
        verify(estoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-08: Deve impedir cadastro quando campo obrigatório principioAtivo não for preenchido")
    void deveImpedirCadastro_QuandoPrincipioAtivoVazio() {
        medicamentoValido.setPrincipioAtivo("");

        assertThrows(IllegalArgumentException.class, () ->
                medicamentoService.cadastrarComEstoque(coordenador, medicamentoValido, estoqueValido)
        );
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-08: Deve impedir cadastro quando campo obrigatório laboratorio não for preenchido")
    void deveImpedirCadastro_QuandoLaboratorioVazio() {
        medicamentoValido.setLaboratorio(null);

        assertThrows(IllegalArgumentException.class, () ->
                medicamentoService.cadastrarComEstoque(coordenador, medicamentoValido, estoqueValido)
        );
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-08: Deve impedir cadastro quando lote não for preenchido")
    void deveImpedirCadastro_QuandoLoteVazio() {
        estoqueValido.setLote("  ");

        assertThrows(IllegalArgumentException.class, () ->
                medicamentoService.cadastrarComEstoque(coordenador, medicamentoValido, estoqueValido)
        );
        verify(estoqueRepository, never()).save(any());
    }

    @DisplayName("US-08: Deve impedir cadastro quando quantidade inicial for menor ou igual a zero")
    void deveImpedirCadastro_QuandoQuantidadeInvalida() {
        estoqueValido.setQuantidade(0);

        assertThrows(IllegalArgumentException.class, () ->
                medicamentoService.cadastrarComEstoque(coordenador, medicamentoValido, estoqueValido)
        );
        verify(estoqueRepository, never()).save(any());
    }
}
