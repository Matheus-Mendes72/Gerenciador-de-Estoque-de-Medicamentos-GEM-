package com.gem;

import com.gem.model.CargoProfissional;
import com.gem.model.Permissao;
import com.gem.model.Usuario;
import com.gem.service.PermissaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TecnicoPermissaoTest {

    private PermissaoService permissaoService;

    private Usuario tecnico;

    @BeforeEach
    void setUp() {

        permissaoService = new PermissaoService();

        tecnico = new Usuario(
                CargoProfissional.FUNCIONARIO_SAT,
                "hashHash"
        );
    }

    @Test
    void devePermitirSaidaParaTecnico() {

        assertTrue(
                permissaoService.possuiPermissao(
                        tecnico,
                        Permissao.REGISTRAR_SAIDA
                )
        );
    }

    @Test
    void devePermitirRetornoParaTecnico() {

        assertTrue(
                permissaoService.possuiPermissao(
                        tecnico,
                        Permissao.REGISTRAR_RETORNO
                )
        );
    }

    @Test
    void deveBloquearCadastroParaTecnico() {

        assertFalse(
                permissaoService.possuiPermissao(
                        tecnico,
                        Permissao.CADASTRAR_MEDICAMENTO
                )
        );
    }

    @Test
    void deveBloquearAtualizacaoParaTecnico() {

        assertFalse(
                permissaoService.possuiPermissao(
                        tecnico,
                        Permissao.ATUALIZAR_MEDICAMENTO
                )
        );
    }

    @Test
    void deveBloquearRemocaoParaTecnico() {

        assertFalse(
                permissaoService.possuiPermissao(
                        tecnico,
                        Permissao.REMOVER_MEDICAMENTO
                )
        );
    }
}