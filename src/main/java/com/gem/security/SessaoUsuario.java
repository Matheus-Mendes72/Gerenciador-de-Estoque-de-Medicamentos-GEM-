package com.gem.security;

import org.springframework.stereotype.Component;

import com.gem.model.Usuario;

@Component
public class SessaoUsuario {

    private Usuario usuarioAtual;

    public void iniciar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                "Não é possível iniciar uma sessão com usuário nulo."
            );
        }

        this.usuarioAtual = usuario;
    }

    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public boolean estaAutenticado() {
        return usuarioAtual != null;
    }

    public void encerrar() {
        usuarioAtual = null;
    }
}