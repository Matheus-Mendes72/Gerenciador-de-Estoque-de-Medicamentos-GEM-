package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gem.model.Historico;
import com.gem.model.TipoMov;

import java.util.List;


public interface HistoricoRepository extends JpaRepository<Historico, Integer> {
    List<Historico> findByTipoMovOrderByDataMovDesc(TipoMov tipoMov);
    List<Historico> findByUsuarioIdOrderByDataMovDesc(Integer usuarioId);
}
