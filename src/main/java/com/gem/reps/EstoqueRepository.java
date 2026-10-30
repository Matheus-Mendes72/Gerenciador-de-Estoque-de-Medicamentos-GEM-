package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gem.model.Estoque;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    List<Estoque> findByPrincipioAtivoIgnoreCase(String principioAtivo);
    Optional<Estoque> findFirstByPrincipioAtivoIgnoreCase(String principioAtivo);
    List<Estoque> findByMedicamentoIdOrderByIdAsc(Integer medicamentoId);

    @Query("SELECT e FROM Estoque e LEFT JOIN FETCH e.medicamento m " +
       "WHERE m IS NULL OR m.ativo = true ORDER BY e.id")
    List<Estoque> findAllAtivos();
}
