package com.gem.dto;

import com.gem.model.SetorHosp;

public class SaidaMedicamentoDTO {

    private String principioAtivo;
    private Integer quantidade;
    private String responsavelRetirada;
    private String medicoPrescritor;
    private SetorHosp setorDestino;
    private String motivo;

    public SaidaMedicamentoDTO() {
    }

    public SaidaMedicamentoDTO(
            String principioAtivo,
            Integer quantidade,
            String responsavelRetirada,
            String medicoPrescritor,
            SetorHosp setorDestino,
            String motivo) {
        this.principioAtivo = principioAtivo;
        this.quantidade = quantidade;
        this.responsavelRetirada = responsavelRetirada;
        this.medicoPrescritor = medicoPrescritor;
        this.setorDestino = setorDestino;
        this.motivo = motivo;
    }

    public void validarCamposObrigatorios() {
        if (principioAtivo == null || principioAtivo.isBlank()) {
            throw new IllegalArgumentException("O princípio ativo do medicamento é obrigatório.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade a ser retirada deve ser maior que zero.");
        }
        if (responsavelRetirada == null || responsavelRetirada.isBlank()) {
            throw new IllegalArgumentException("O nome do responsável pela retirada é obrigatório.");
        }
        if (medicoPrescritor == null || medicoPrescritor.isBlank()) {
            throw new IllegalArgumentException("O médico prescritor é obrigatório.");
        }
        if (setorDestino == null) {
            throw new IllegalArgumentException("O setor de destino da medicação é obrigatório.");
        }
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getResponsavelRetirada() {
        return responsavelRetirada;
    }

    public void setResponsavelRetirada(String responsavelRetirada) {
        this.responsavelRetirada = responsavelRetirada;
    }

    public String getMedicoPrescritor() {
        return medicoPrescritor;
    }

    public void setMedicoPrescritor(String medicoPrescritor) {
        this.medicoPrescritor = medicoPrescritor;
    }

    public SetorHosp getSetorDestino() {
        return setorDestino;
    }

    public void setSetorDestino(SetorHosp setorDestino) {
        this.setorDestino = setorDestino;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
