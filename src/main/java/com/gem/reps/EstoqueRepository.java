
package com.gem.reps;

import com.gem.model.Estoque;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {

    List<Estoque> findByMedicamentoIdOrderByIdAsc(Integer medicamentoId);
}
