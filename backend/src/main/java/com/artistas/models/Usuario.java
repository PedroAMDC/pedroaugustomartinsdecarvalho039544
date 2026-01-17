package com.artistas.models;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntity {

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    public String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @NotBlank
    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public Boolean ativo = true;

    @Column(name = "created_at", updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean verifyPassword(String plainPassword) {
        return BcryptUtil.matches(plainPassword, this.passwordHash);
    }

    public static Usuario create(String email, String password, String nome) {
        Usuario usuario = new Usuario();
        usuario.email = email;
        usuario.passwordHash = BcryptUtil.bcryptHash(password);
        usuario.nome = nome;
        usuario.ativo = true;
        return usuario;
    }

    public static Usuario findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
