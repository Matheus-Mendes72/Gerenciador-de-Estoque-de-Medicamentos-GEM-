package com.gem.security;

import com.gem.model.CargoProfissional;
import com.gem.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class SessaoUsuario {

    private Usuario usuarioAtual;

    public void iniciar(Usuario usuario) {
        this.usuarioAtual = usuario;
    }

    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    public boolean estaAutenticado() {
        return usuarioAtual != null;
    }

    public boolean ehFarmaceutico() {
        return estaAutenticado()
                && usuarioAtual.getCargo() == CargoProfissional.FUNCIONARIO_CAF;
    }

    public boolean ehCoordenador() {
        return estaAutenticado()
                && usuarioAtual.getCargo() == CargoProfissional.COORDENADOR;
    }

    public void encerrar() {
        usuarioAtual = null;
    }
}