package com.gem.service;

import com.gem.model.Historico;
import com.gem.model.Permissao;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.reps.HistoricoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true) // garante que nenhuma operação de escrita pode ocorrer aqui
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final PermissaoService permissaoService;

    public HistoricoService(
            HistoricoRepository historicoRepository,
            PermissaoService permissaoService) {

        this.historicoRepository = historicoRepository;
        this.permissaoService = permissaoService;
    }

    /**
     * Consulta o histórico de movimentações com filtros opcionais.
     * Exclusivo para COORDENADOR — lança exceção se outro cargo tentar acessar.
     *
     * @param usuario        usuário logado (deve ser COORDENADOR)
     * @param tipoMov        filtra por tipo de movimentação; null = todos os tipos
     * @param dataInicio     filtra movimentações a partir desta data; null = sem limite inferior
     * @param dataFim        filtra movimentações até esta data; null = sem limite superior
     * @param principioAtivo filtra pelo princípio ativo do medicamento; null ou vazio = todos
     * @return lista de históricos ordenada da movimentação mais recente para a mais antiga
     */
    public List<Historico> consultar(
            Usuario usuario,
            TipoMov tipoMov,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String principioAtivo) {

        // Valida que somente o COORDENADOR pode consultar o histórico
        permissaoService.exigirPermissao(usuario, Permissao.CONSULTAR_HISTORICO);

        // Normaliza string vazia para null, para que o filtro seja ignorado no banco
        // E formata para o LIKE
        String filtroAtivo = (principioAtivo != null && !principioAtivo.isBlank())
                ? "%" + principioAtivo.toLowerCase() + "%"
                : null;

        return historicoRepository.buscarComFiltros(tipoMov, dataInicio, dataFim, filtroAtivo);
    }
}
