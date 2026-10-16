
package com.gem.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medicamento", nullable = false)
    private Medicamento medicamento;

    @Column(name = "principio_ativo", length = 50)
    private String principioAtivo;

    @Column(name = "dose", length = 50)
    private String dose;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_med", columnDefinition = "tipo_med")
    private TipoMed tipoMed;

    @Column(name = "lote", length = 50)
    private String lote;

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

    public String getPrincipioAtivo() {
        if (principioAtivo != null) {
            return principioAtivo;
        }
        return medicamento != null ? medicamento.getPrincipioAtivo() : null;
    }

    public String getDose() {
        if (dose != null) {
            return dose;
        }
        return medicamento != null ? medicamento.getDose() : null;
    }

    public TipoMed getTipoMed() {
        if (tipoMed != null) {
            return tipoMed;
        }
        return medicamento != null ? medicamento.getTipoMed() : null;
    }

    public String getLote() {
        return lote;
    }

    public Integer getQuantidade() {
        return quantidade;
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
