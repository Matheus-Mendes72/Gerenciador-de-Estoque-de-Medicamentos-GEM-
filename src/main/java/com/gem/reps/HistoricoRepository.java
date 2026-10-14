package com.gem.reps;

import com.gem.model.Historico;
import com.gem.model.TipoMov;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoricoRepository extends JpaRepository<Historico, Integer> {

    List<Historico> findByTipoMovOrderByDataMovDesc(TipoMov tipoMov);

    List<Historico> findByUsuarioIdOrderByDataMovDesc(Integer usuarioId);

    /**
     * Busca o histórico aplicando até três filtros opcionais de forma combinada.
     * Quando um parâmetro é nulo, o filtro correspondente é ignorado.
     *
     * @param tipoMov       filtra pelo tipo de movimentação (SAIDA, RETORNO...)
     * @param dataInicio    filtra movimentações a partir desta data/hora
     * @param dataFim       filtra movimentações até esta data/hora
     * @param principioAtivo filtra pelo princípio ativo do medicamento (busca parcial, case-insensitive)
     */
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

