package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gem.model.*;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Pra consultar na tabela de Usuarios de acordo com o cargo
    Optional<Usuario> findByCargo(CargoProfissional cargo);
}
