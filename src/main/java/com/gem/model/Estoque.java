
package com.gem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    // @Column(name = "lote", nullable = false, length = 50)
    // private String lote;

    // @Column(name = "data_validade", nullable = false)
    // private LocalDate dataValidade;

    @Column(nullable = false)
    private Integer quantidade;

    public Estoque() {
    }

    public Integer getId() {
        return id;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    // public String getLote() {
    //     return lote;
    // }

    // public LocalDate getDataValidade() {
    //     return dataValidade;
    // }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    // public void setLote(String lote) {
    //     this.lote = lote;
    // }

    // public void setDataValidade(LocalDate dataValidade) {
    //     this.dataValidade = dataValidade;
    // }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
