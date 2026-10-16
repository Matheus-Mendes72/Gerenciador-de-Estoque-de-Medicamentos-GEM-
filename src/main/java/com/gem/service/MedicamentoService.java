
package com.gem.service;

import com.gem.model.Estoque;
import com.gem.model.Medicamento;
import com.gem.reps.EstoqueRepository;
import com.gem.reps.MedicamentoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final EstoqueRepository estoqueRepository;

    public MedicamentoService(
            MedicamentoRepository medicamentoRepository,
            EstoqueRepository estoqueRepository) {

        this.medicamentoRepository = medicamentoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Transactional
    public void cadastrarComEstoque(
            Medicamento medicamento,
            Estoque estoque) {

        // 1. Salva o medicamento no catálogo
        Medicamento medicamentoSalvo =
                medicamentoRepository.save(medicamento);

        // 2. Associa o lote ao medicamento salvo
        // estoque.setMedicamento(medicamentoSalvo);

        // 3. Salva o lote no estoque
        estoqueRepository.save(estoque);
    }

}
