package com.gem.reps;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gem.model.Medicamento;

public interface MedicamentoRepository
        extends JpaRepository<Medicamento, Integer> {

    List<Medicamento> findByAtivoTrue();

    List<Medicamento>
        findByAtivoTrueAndPrincipioAtivoContainingIgnoreCase(
            String principioAtivo
        );
}