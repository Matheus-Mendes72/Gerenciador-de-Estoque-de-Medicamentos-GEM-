package com.gem.controller;

import com.gem.model.Historico;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.service.HistoricoService;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    /**
     * Consulta o histórico de movimentações com filtros opcionais.
     * Parâmetros nulos são tratados como "sem filtro" pelo service e pelo repositório.
     *
     * @param usuario        usuário logado (deve ser COORDENADOR)
     * @param tipoMov        tipo de movimentação para filtrar; null = todos
     * @param dataInicio     data/hora inicial do período; null = sem limite inferior
     * @param dataFim        data/hora final do período; null = sem limite superior
     * @param principioAtivo princípio ativo do medicamento para filtrar; null = todos
     * @return lista de históricos ordenada da mais recente para a mais antiga
     */
    public List<Historico> consultar(
            Usuario usuario,
            TipoMov tipoMov,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String principioAtivo) {

        return historicoService.consultar(usuario, tipoMov, dataInicio, dataFim, principioAtivo);
    }
}
