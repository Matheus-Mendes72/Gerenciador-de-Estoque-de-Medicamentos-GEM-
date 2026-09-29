package com.gem;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.reps.UsuarioRepository;
import com.gem.service.AutenticacaoService;

public class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deveAutenticarComCredenciaisCorretas() {
        // Simula um usuário salvo no banco com senha criptografada fictícia
        Usuario usuarioMock = new Usuario(CargoProfissional.COORDENADOR, "$2a$10$exemploHashSenha");
        
        when(usuarioRepository.findByCargo(CargoProfissional.COORDENADOR)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("senha123", usuarioMock.getSenhaHash())).thenReturn(true);

        // Executa o método de autenticação
        Usuario resultado = autenticacaoService.autenticar(CargoProfissional.COORDENADOR, "senha123");

        assertNotNull(resultado);
    }

    @Test
    public void deveNegarAcessoComSenhaIncorreta() {
        Usuario usuarioMock = new Usuario(CargoProfissional.COORDENADOR, "$2a$10$exemploHashSenha");
        
        when(usuarioRepository.findByCargo(CargoProfissional.COORDENADOR)).thenReturn(Optional.of(usuarioMock));
        when(passwordEncoder.matches("senhaErrada", usuarioMock.getSenhaHash())).thenReturn(false);

        // Deve lançar a exceção de credenciais inválidas
        assertThrows(BadCredentialsException.class, () -> {
            autenticacaoService.autenticar(CargoProfissional.COORDENADOR, "senhaErrada");
        });
    }
}