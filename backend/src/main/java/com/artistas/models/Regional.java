package com.artistas.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "regionais")
public class Regional extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotNull
    @Column(name = "external_id", nullable = false)
    public Integer externalId;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    public String nome;

    @Column(nullable = false)
    public Boolean ativo = true;

    public static Regional create(Integer externalId, String nome) {
        Regional regional = new Regional();
        regional.externalId = externalId;
        regional.nome = nome;
        regional.ativo = true;
        return regional;
    }

    public static Regional create(Integer externalId, String nome, Boolean ativo) {
        Regional regional = new Regional();
        regional.externalId = externalId;
        regional.nome = nome;
        regional.ativo = ativo;
        return regional;
    }

    public static Regional findByExternalId(Integer externalId) {
        return find("externalId = ?1 and ativo = true", externalId).firstResult();
    }

    public static Regional findByNome(String nome) {
        return find("nome", nome).firstResult();
    }

    public static List<Regional> findAllAtivos() {
        return list("ativo", true);
    }

    public static List<Regional> findByExternalIdAll(Integer externalId) {
        return list("externalId", externalId);
    }
}
