package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gem.model.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    List<Estoque> findByPrincipioAtivoIgnoreCase(String principioAtivo);
    Optional<Estoque> findFirstByPrincipioAtivoIgnoreCase(String principioAtivo);
}
