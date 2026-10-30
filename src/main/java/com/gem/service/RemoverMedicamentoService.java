package com.gem.service;

import com.gem.model.Estoque;
import com.gem.model.Historico;
import com.gem.model.Medicamento;
import com.gem.model.Permissao;
import com.gem.model.SetorHosp;
import com.gem.model.TipoMov;
import com.gem.model.Usuario;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.HistoricoRepository;
import com.gem.reps.MedicamentoRepository;
import com.gem.security.SessaoUsuario;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoverMedicamentoService {

    /** Tamanho da coluna historico.motivo_obs (VARCHAR(100)). */
    public static final int MOTIVO_MAX_LENGTH = 100;

    private static final String PREFIXO_CATALOGO = "REMOÇÃO DO CATÁLOGO: ";

    private final EstoqueRepository estoqueRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final HistoricoRepository historicoRepository;
    private final SessaoUsuario sessaoUsuario;
    private final PermissaoService permissaoService;

    public RemoverMedicamentoService(
            EstoqueRepository estoqueRepository,
            MedicamentoRepository medicamentoRepository,
            HistoricoRepository historicoRepository,
            SessaoUsuario sessaoUsuario,
            PermissaoService permissaoService) {

        this.estoqueRepository = estoqueRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.historicoRepository = historicoRepository;
        this.sessaoUsuario = sessaoUsuario;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentosDisponiveis() {
        return medicamentoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Medicamento> buscarPorPrincipioAtivo(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarMedicamentosDisponiveis();
        }
        return medicamentoRepository.findByAtivoTrueAndPrincipioAtivoContainingIgnoreCase(termo.trim());
    }

    @Transactional(readOnly = true)
    public int obterQuantidadeDisponivel(Medicamento medicamento) {
        validarMedicamento(medicamento);

        return somarSaldo(
                estoqueRepository.findByMedicamentoIdOrderByIdAsc(medicamento.getId())
        );
    }

    @Transactional
    public Medicamento removerDoCatalogo(
            Usuario usuario,
            Integer medicamentoId,
            String motivo) {

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REMOVER_MEDICAMENTO
        );

        if (medicamentoId == null) {
            throw new IllegalArgumentException("Identificador do medicamento é obrigatório.");
        }

        // Validado antes de qualquer escrita, para não falhar no meio da transação
        // por estouro da coluna motivo_obs.
        String observacao = (motivo != null && !motivo.isBlank())
                ? PREFIXO_CATALOGO + motivo.trim()
                : "REMOÇÃO DO CATÁLOGO PELO COORDENADOR";
        validarTamanhoMotivo(observacao, MOTIVO_MAX_LENGTH - PREFIXO_CATALOGO.length());

        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));

        if (!Boolean.TRUE.equals(medicamento.getAtivo())) {
            throw new IllegalStateException("Medicamento já se encontra inativo no catálogo.");
        }

        // 1. Inativa o medicamento do catálogo (soft delete)
        medicamento.setAtivo(false);
        Medicamento medicamentoSalvo = medicamentoRepository.save(medicamento);

        // 2. Registra saída e baixa para os estoques vinculados ainda existentes
        List<Estoque> estoques = estoqueRepository.findByMedicamentoIdOrderByIdAsc(medicamentoId);

        for (Estoque estoque : estoques) {
            Integer qtdAtual = estoque.getQuantidade();
            if (qtdAtual != null && qtdAtual > 0) {
                estoque.setQuantidade(0);
                estoqueRepository.save(estoque);

                registrarHistoricoRemocao(usuario, estoque, qtdAtual, 0, observacao);
            }
        }

        return medicamentoSalvo;
    }

    @Transactional
    public Medicamento removerDoCatalogo(Integer medicamentoId, String motivo) {
        Usuario usuario = sessaoUsuario.getUsuarioAtual();
        return removerDoCatalogo(usuario, medicamentoId, motivo);
    }

    @Transactional
    public void removerEstoque(
            Medicamento medicamento,
            Integer quantidadeRetirada,
            String motivo) {

        Usuario usuario = sessaoUsuario.getUsuarioAtual();

        permissaoService.exigirPermissao(
                usuario,
                Permissao.REMOVER_MEDICAMENTO
        );

        validarMedicamento(medicamento);

        if (quantidadeRetirada == null || quantidadeRetirada <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser um inteiro positivo.");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo da remoção é obrigatório.");
        }

        String motivoLimpo = motivo.trim();
        validarTamanhoMotivo(motivoLimpo, MOTIVO_MAX_LENGTH);

        Medicamento medicamentoPersistido =
                medicamentoRepository.findById(medicamento.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Medicamento não encontrado."));

        if (!Boolean.TRUE.equals(medicamentoPersistido.getAtivo())) {
            throw new IllegalStateException("Não é possível remover estoque de um medicamento inativo.");
        }

        List<Estoque> estoques =
                estoqueRepository.findByMedicamentoIdOrderByIdAsc(medicamentoPersistido.getId());

        int quantidadeDisponivel = somarSaldo(estoques);

        if (quantidadeDisponivel <= 0) {
            throw new IllegalArgumentException(
                    "Medicamento sem saldo em estoque. Remova-o do catálogo."
            );
        }

        if (quantidadeDisponivel < quantidadeRetirada) {
            throw new IllegalArgumentException("Estoque insuficiente. Disponível: " + quantidadeDisponivel + ".");
        }

        // Baixa distribuída entre os lotes, do mais antigo para o mais novo.
        int restante = quantidadeRetirada;

        for (Estoque estoque : estoques) {
            if (restante == 0) {
                break;
            }

            Integer quantidadeAtual = estoque.getQuantidade();
            if (quantidadeAtual == null || quantidadeAtual <= 0) {
                continue;
            }

            int retirada = Math.min(quantidadeAtual, restante);
            int posterior = quantidadeAtual - retirada;

            estoque.setQuantidade(posterior);
            estoqueRepository.save(estoque);

            registrarHistoricoRemocao(usuario, estoque, retirada, posterior, quantidadeAtual, motivoLimpo);

            restante -= retirada;
        }

        // Calculado a partir do saldo já conhecido, sem depender de nova leitura.
        boolean existeSaldo = quantidadeDisponivel > quantidadeRetirada;

        if (!existeSaldo) {
            medicamentoPersistido.setAtivo(false);
            medicamentoRepository.save(medicamentoPersistido);
        }
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private int somarSaldo(List<Estoque> estoques) {
        return estoques.stream()
                .filter(estoque -> estoque.getQuantidade() != null)
                .mapToInt(Estoque::getQuantidade)
                .filter(quantidade -> quantidade > 0)
                .sum();
    }

    private void validarTamanhoMotivo(String texto, int maximoParaMotivo) {
        if (texto.length() > MOTIVO_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O motivo deve ter no máximo " + maximoParaMotivo + " caracteres."
            );
        }
    }

    /** Remoção total de um lote (catálogo): quantidade removida = saldo anterior. */
    private void registrarHistoricoRemocao(
            Usuario usuario,
            Estoque estoque,
            int quantidadeRemovida,
            int quantidadePos,
            String observacao) {

        registrarHistoricoRemocao(usuario, estoque, quantidadeRemovida, quantidadePos, quantidadeRemovida, observacao);
    }

    private void registrarHistoricoRemocao(
            Usuario usuario,
            Estoque estoque,
            int quantidadeRemovida,
            int quantidadePos,
            int quantidadePre,
            String observacao) {

        Historico historico = new Historico();
        historico.setTipoMov(TipoMov.SAIDA);
        historico.setEstoque(estoque);
        historico.setQuantidade(quantidadeRemovida);
        historico.setDataMov(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setSetorHosp(SetorHosp.EMERGENCIA);
        historico.setNomeFunc(
                usuario != null && usuario.getCargo() != null
                        ? usuario.getCargo().name()
                        : "COORDENADOR"
        );
        historico.setMedicoPrescritor("NÃO APLICÁVEL (REMOÇÃO)");
        historico.setMotivoObs(observacao);
        historico.setQuantidadePre(quantidadePre);
        historico.setQuantidadePos(quantidadePos);

        historicoRepository.save(historico);
    }

    private void validarMedicamento(Medicamento medicamento) {
        if (medicamento == null || medicamento.getId() == null) {
            throw new IllegalArgumentException("Selecione um medicamento válido.");
        }
    }
}