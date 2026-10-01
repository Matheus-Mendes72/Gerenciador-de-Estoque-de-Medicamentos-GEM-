package com.gem.model;

// Falta adicionar uma lista de históricos dentro dessa Entidade

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;


@Entity 
@Table(name = "usuarios")
public class Usuario {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    // Pelo o que pesquisei, informa ao Hibernate que está sendo trabalhado com Enum nativo do PostgreSQL
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "cargo", nullable = false, columnDefinition = "cargo")
    private CargoProfissional cargo;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    // Útil para o JPA criar a entidade sem necessariamente passar os atributos pelo construtor
    public Usuario() {
    }

    public Usuario(CargoProfissional cargo, String senhaHash) {
        this.cargo = cargo;
        this.senhaHash = senhaHash;
    }

    public Integer getId() {
        return this.id;
    }

    public CargoProfissional getCargo() {
        return this.cargo;
    }

    // OBS para verificar com POO se realmente é necessário dar a possibilidade de mudar um cargo já estabelicido
    public void setCargo(CargoProfissional cargo) {
        this.cargo = cargo;
    }

    public String getSenhaHash() {
        return this.senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}
