package com.gem.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.reps.UsuarioRepository;

@Component 
public class UsuarioInicializador implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${GEM_SENHA_COORDENADOR:}")
    private String senhaCoordenador;

    @Value("${GEM_SENHA_CAF:}")
    private String senhaCaf;

    @Value("${GEM_SENHA_SAT:}")
    private String senhaSat;

    public UsuarioInicializador(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void criarUsuario(CargoProfissional cargo, String senha) {
        if (senha == null || senha.isBlank()) {
            return;
        }

        if (usuarioRepository.findByCargo(cargo).isEmpty()) {
            Usuario usuario = new Usuario(cargo, passwordEncoder.encode(senha));

            usuarioRepository.save(usuario);
        }
    }

    @Override
    public void run(String... args) {

        criarUsuario(CargoProfissional.COORDENADOR, senhaCoordenador);

        criarUsuario(CargoProfissional.FUNCIONARIO_CAF, senhaCaf);

        criarUsuario(CargoProfissional.FUNCIONARIO_SAT, senhaSat);
    }
}