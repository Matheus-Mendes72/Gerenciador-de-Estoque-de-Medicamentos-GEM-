package com.gem.service;

import org.springframework.stereotype.Service;

import com.gem.model.CargoProfissional;
import com.gem.model.Permissao;
import com.gem.model.Usuario;

@Service
public class PermissaoService {

    public boolean possuiPermissao(
            Usuario usuario,
            Permissao permissao) {

        if (usuario == null || usuario.getCargo() == null) {
            return false;
        }

        if (usuario.getCargo() == CargoProfissional.COORDENADOR) {
            return true;
        }

        if (usuario.getCargo() == CargoProfissional.FUNCIONARIO_CAF) {
            return permissao == Permissao.REGISTRAR_SAIDA
                    || permissao == Permissao.REGISTRAR_RETORNO
                    || permissao == Permissao.ATUALIZAR_MEDICAMENTO;
        }

        if (usuario.getCargo() == CargoProfissional.FUNCIONARIO_SAT) {
            return permissao == Permissao.REGISTRAR_SAIDA
                    || permissao == Permissao.REGISTRAR_RETORNO;
        }

        return false;
    }

    public void exigirPermissao(
            Usuario usuario,
            Permissao permissao) {

        if (!possuiPermissao(usuario, permissao)) {
            throw new IllegalArgumentException(
                "Usuário sem permissão para realizar esta operação."
            );
        }
    }

    public boolean podeAcessarRetorno(Usuario usuario) {
        return possuiPermissao(usuario, Permissao.REGISTRAR_RETORNO);
    }

    public void exigirAcessoRetorno(Usuario usuario) {
        if (!podeAcessarRetorno(usuario)) {
            throw new SecurityException(
                "Acesso negado: Usuário sem permissão para acessar a funcionalidade de retorno de medicamentos."
            );
        }
    }
}