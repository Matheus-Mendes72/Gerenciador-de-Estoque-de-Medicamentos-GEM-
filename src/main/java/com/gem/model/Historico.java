package com.gem.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
@Entity
@Table(name = "historico")
public class Historico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_mov", nullable = false, columnDefinition = "tipo_mov")
    private TipoMov tipoMov;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estoque", nullable = false)
    private Estoque estoque;
    @Column(nullable = false)
    private Integer quantidade;
    @Column(name = "data_mov", nullable = false)
    private LocalDateTime dataMov;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "setor_hosp", nullable = false, columnDefinition = "setor_hosp")
    private SetorHosp setorHosp;

    @Column(name = "nome_func", nullable = false, length = 50)
    private String nomeFunc;
    @Column(name = "motivo_obs", length = 100)
    private String motivoObs;
    @Column(name = "quantidade_pre", nullable = false)
    private Integer quantidadePre;
    @Column(name = "quantidade_pos", nullable = false)
    private Integer quantidadePos;
    public Historico() {
    }
    public Integer getId() {
        return id;
    }
    public TipoMov getTipoMov() {
        return tipoMov;
    }
    public Estoque getEstoque() {
        return estoque;
    }
    public Integer getQuantidade() {
        return quantidade;
    }
    public LocalDateTime getDataMov() {
        return dataMov;
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public SetorHosp getSetorHosp() {
        return setorHosp;
    }
    public String getNomeFunc() {
        return nomeFunc;
    }
    public String getMotivoObs() {
        return motivoObs;
    }
    public Integer getQuantidadePre() {
        return quantidadePre;
    }
    public Integer getQuantidadePos() {
        return quantidadePos;
    }
    public void setTipoMov(TipoMov tipoMov) {
        this.tipoMov = tipoMov;
    }
    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }
    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
    public void setDataMov(LocalDateTime dataMov) {
        this.dataMov = dataMov;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public void setSetorHosp(SetorHosp setorHosp) {
        this.setorHosp = setorHosp;
    }
    public void setNomeFunc(String nomeFunc) {
        this.nomeFunc = nomeFunc;
    }
    public void setMotivoObs(String motivoObs) {
        this.motivoObs = motivoObs;
    }
    public void setQuantidadePre(Integer quantidadePre) {
        this.quantidadePre = quantidadePre;
    }

    @Column(name = "nome_func", nullable = false, length = 50)
    private String nomeFunc;

    @Column(name = "motivo_obs", length = 100)
    private String motivoObs;

    @Column(name = "quantidade_pre", nullable = false)
    private Integer quantidadePre;

    @Column(name = "quantidade_pos", nullable = false)
    private Integer quantidadePos;

    public Historico() {
    }

    public Integer getId() {
        return id;
    }

    public TipoMov getTipoMov() {
        return tipoMov;
    }

    public Estoque getEstoque() {
        return estoque;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataMov() {
        return dataMov;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public SetorHosp getSetorHosp() {
        return setorHosp;
    }

    public String getNomeFunc() {
        return nomeFunc;
    }

    public String getMotivoObs() {
        return motivoObs;
    }

    public Integer getQuantidadePre() {
        return quantidadePre;
    }

    public Integer getQuantidadePos() {
        return quantidadePos;
    }

    public void setTipoMov(TipoMov tipoMov) {
        this.tipoMov = tipoMov;
    }

    public void setEstoque(Estoque estoque) {
        this.estoque = estoque;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void setDataMov(LocalDateTime dataMov) {
        this.dataMov = dataMov;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setSetorHosp(SetorHosp setorHosp) {
        this.setorHosp = setorHosp;
    }

    public void setNomeFunc(String nomeFunc) {
        this.nomeFunc = nomeFunc;
    }

    public void setMotivoObs(String motivoObs) {
        this.motivoObs = motivoObs;
    }

    public void setQuantidadePre(Integer quantidadePre) {
        this.quantidadePre = quantidadePre;
    }

    public void setQuantidadePos(Integer quantidadePos) {
        this.quantidadePos = quantidadePos;
    }
}