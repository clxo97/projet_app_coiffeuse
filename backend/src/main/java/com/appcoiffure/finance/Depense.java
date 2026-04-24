package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.appcoiffure.coiffeuse.Coiffeuse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "depenses")
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coiffeuse_id", nullable = false)
    private Coiffeuse coiffeuse;

    @Column(nullable = false)
    private LocalDate dateDepense;

    @Column(nullable = false, length = 140)
    private String libelle;

    @Column(nullable = false, length = 80)
    private String categorie;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    @Column(nullable = false)
    private Instant modifieLe;

    protected Depense() {
    }

    public Depense(Coiffeuse coiffeuse, LocalDate dateDepense, String libelle, String categorie, BigDecimal montant, String notes) {
        this.coiffeuse = coiffeuse;
        this.dateDepense = dateDepense;
        this.libelle = libelle;
        this.categorie = categorie;
        this.montant = montant;
        this.notes = notes;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        creeLe = now;
        modifieLe = now;
    }

    @PreUpdate
    void preUpdate() {
        modifieLe = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Coiffeuse getCoiffeuse() {
        return coiffeuse;
    }

    public LocalDate getDateDepense() {
        return dateDepense;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getCategorie() {
        return categorie;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void modifier(LocalDate dateDepense, String libelle, String categorie, BigDecimal montant, String notes) {
        this.dateDepense = dateDepense;
        this.libelle = libelle;
        this.categorie = categorie;
        this.montant = montant;
        this.notes = notes;
    }

    public void attribuerCoiffeuse(Coiffeuse coiffeuse) {
        this.coiffeuse = coiffeuse;
    }
}
