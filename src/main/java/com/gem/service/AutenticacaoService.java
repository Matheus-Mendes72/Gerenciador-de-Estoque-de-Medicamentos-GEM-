package com.gem.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import com.gem.reps.UsuarioRepository;

@Service 
public class AutenticacaoService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario autenticar(CargoProfissional cargo, String senha) {
        Usuario usuario = usuarioRepository.findByCargo(cargo)
            .orElseThrow(() -> new BadCredentialsException("Cargo ou senha inválidos"));

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw new BadCredentialsException("Cargo ou senha inválidos");
        }

        return usuario;
    }
}
