package com.gem.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity 
@Table(name = "estoque")
public class Estoque {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "principio_ativo", nullable = false, length = 50)
    private String principioAtivo;

    @Column(nullable = false, length = 50)
    private String dose;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_med", nullable = false, columnDefinition = "tipo_med")
    private TipoMed tipoMed;

    @Column(nullable = false)
    private Integer quantidade;

    public Estoque() {
    }

    public Integer getId() {
        return this.id;
    }

    public String getPrincipioAtivo() {
        return this.principioAtivo;
    }

    public String getDose() {
        return this.dose;
    }

    public TipoMed getTipoMed() {
        return this.tipoMed;
    }

    public Integer getQuantidade() {
        return this.quantidade;
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

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}