package com.appcoiffure.client;

import java.time.Instant;

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
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "coiffeuse_id", nullable = false)
    private Coiffeuse coiffeuse;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 40)
    private String telephone;

    @Column(length = 180)
    private String email;

    @Column(length = 80)
    private String instagram;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean smsActif = true;

    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    @Column(nullable = false)
    private Instant modifieLe;

    protected Client() {
    }

    public Client(Coiffeuse coiffeuse, String nom, String telephone, String email, String instagram, String notes, Boolean smsActif) {
        this.coiffeuse = coiffeuse;
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        this.instagram = instagram;
        this.notes = notes;
        this.smsActif = smsActif == null || smsActif;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        creeLe = now;
        modifieLe = now;

        if (smsActif == null) {
            smsActif = true;
        }
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

    public String getNom() {
        return nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getNotes() {
        return notes;
    }

    public Boolean getSmsActif() {
        return smsActif == null || smsActif;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public void modifier(String nom, String telephone, String email, String instagram, String notes, Boolean smsActif) {
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        this.instagram = instagram;
        this.notes = notes;
        this.smsActif = smsActif == null || smsActif;
    }

    public void attribuerCoiffeuse(Coiffeuse coiffeuse) {
        this.coiffeuse = coiffeuse;
    }
}
