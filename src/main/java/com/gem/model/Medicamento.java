package com.gem.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "medicamentos")
public class Medicamento {

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
    private Boolean ativo = true;

    @Column(name = "laboratorio", length = 100)
    private String laboratorio;

    public Medicamento() {
    }

    public Integer getId() {
        return id;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public String getDose() {
        return dose;
    }

    public TipoMed getTipoMed() {
        return tipoMed;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getLaboratorio() {
        return laboratorio;
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

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public void setId(Integer id) { 
        this.id = id; 
    }
}
