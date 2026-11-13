package com.gem.reps;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gem.model.Historico;
import com.gem.model.TipoMov;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoricoRepository extends JpaRepository<Historico, Integer> {
    List<Historico> findByTipoMovOrderByDataMovDesc(TipoMov tipoMov);
    List<Historico> findByUsuarioIdOrderByDataMovDesc(Integer usuarioId);

    @Query("SELECT h FROM Historico h JOIN FETCH h.estoque " +
           "WHERE (CAST(:tipoMov AS text) IS NULL OR h.tipoMov = :tipoMov) " +
           "AND (CAST(:dataInicio AS timestamp) IS NULL OR h.dataMov >= :dataInicio) " +
           "AND (CAST(:dataFim AS timestamp) IS NULL OR h.dataMov <= :dataFim) " +
           "AND (CAST(:principioAtivo AS text) IS NULL OR LOWER(h.estoque.principioAtivo) LIKE :principioAtivo) " +
           "ORDER BY h.dataMov DESC")
    List<Historico> buscarComFiltros(
            @Param("tipoMov") TipoMov tipoMov,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("principioAtivo") String principioAtivo);
}
