package com.gem.service;

import org.springframework.stereotype.Component;

import com.gem.model.Usuario;

@Component
public class SessaoUsuario {

    private Usuario usuario;

    public void iniciar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                "Não é possível iniciar uma sessão com usuário nulo."
            );
        }

        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }

    public void encerrar() {
        this.usuario = null;
    }
}