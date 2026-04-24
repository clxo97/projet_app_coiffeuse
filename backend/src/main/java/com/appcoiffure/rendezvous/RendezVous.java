package com.appcoiffure.rendezvous;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.appcoiffure.client.Client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false, columnDefinition = "int default 60")
    private Integer dureeMinutes;

    @Column(nullable = false, length = 140)
    private String prestation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutRendezVous statut;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean rappelEnvoye = false;

    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    @Column(nullable = false)
    private Instant modifieLe;

    protected RendezVous() {
    }

    public RendezVous(
            Client client,
            LocalDateTime dateHeure,
            Integer dureeMinutes,
            String prestation,
            BigDecimal prix,
            StatutRendezVous statut,
            String notes
    ) {
        this.client = client;
        this.dateHeure = dateHeure;
        this.dureeMinutes = dureeMinutes;
        this.prestation = prestation;
        this.prix = prix;
        this.statut = statut;
        this.notes = notes;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        creeLe = now;
        modifieLe = now;

        if (dureeMinutes == null) {
            dureeMinutes = 60;
        }

        if (rappelEnvoye == null) {
            rappelEnvoye = false;
        }
    }

    @PreUpdate
    void preUpdate() {
        modifieLe = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes == null ? 60 : dureeMinutes;
    }

    public String getPrestation() {
        return prestation;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public StatutRendezVous getStatut() {
        return statut;
    }

    public String getNotes() {
        return notes;
    }

    public Boolean getRappelEnvoye() {
        return rappelEnvoye != null && rappelEnvoye;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void modifier(
            Client client,
            LocalDateTime dateHeure,
            Integer dureeMinutes,
            String prestation,
            BigDecimal prix,
            StatutRendezVous statut,
            String notes
    ) {
        this.client = client;
        this.dateHeure = dateHeure;
        this.dureeMinutes = dureeMinutes;
        this.prestation = prestation;
        this.prix = prix;
        this.statut = statut;
        this.notes = notes;
    }

    public void marquerRappelEnvoye() {
        this.rappelEnvoye = true;
    }
}
