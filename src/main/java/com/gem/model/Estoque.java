package com.gem.model;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity 
@Table(name = "estoque")
public class Estoque {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medicamento")
    private Medicamento medicamento;

    @Column(name = "principio_ativo", length = 50)
    private String principioAtivo;

    @Column(length = 50)
    private String dose;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_med", columnDefinition = "tipo_med")
    private TipoMed tipoMed;

    @Column(name = "lote", length = 50)
    private String lote;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    public LocalDate getDataValidade() { return this.dataValidade; }

    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public Estoque() {
    }

    public Integer getId() {
        return this.id;
    }

    public Medicamento getMedicamento() {
        return this.medicamento;
    }

    public String getPrincipioAtivo() {
        if (this.principioAtivo != null) {
            return this.principioAtivo;
        }
        return this.medicamento != null ? this.medicamento.getPrincipioAtivo() : null;
    }

    public String getDose() {
        if (this.dose != null) {
            return this.dose;
        }
        return this.medicamento != null ? this.medicamento.getDose() : null;
    }

    public TipoMed getTipoMed() {
        if (this.tipoMed != null) {
            return this.tipoMed;
        }
        return this.medicamento != null ? this.medicamento.getTipoMed() : null;
    }

    public String getLote() {
        return this.lote;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
        if (medicamento != null) {
            if (this.principioAtivo == null) {
                this.principioAtivo = medicamento.getPrincipioAtivo();
            }
            if (this.dose == null) {
                this.dose = medicamento.getDose();
            }
            if (this.tipoMed == null) {
                this.tipoMed = medicamento.getTipoMed();
            }
        }
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public void setTipoMed(TipoMed tipoMed) {
        this.tipoMed = tipoMed;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}
