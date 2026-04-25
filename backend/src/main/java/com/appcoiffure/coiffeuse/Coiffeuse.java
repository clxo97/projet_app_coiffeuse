package com.appcoiffure.coiffeuse;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "coiffeuses")
public class Coiffeuse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nom;

    @Column(length = 160)
    private String nomSalon;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus subscriptionStatus;

    private Instant abonnementActifJusquAu;

    @Column(length = 120)
    private String stripeCustomerId;

    @Column(length = 120)
    private String stripeSubscriptionId;

    @Column(length = 500)
    private String modeleSmsConfirmation;

    @Column(length = 500)
    private String modeleSmsModification;

    @Column(length = 500)
    private String modeleSmsRappel;

    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    @Column(nullable = false)
    private Instant modifieLe;

    protected Coiffeuse() {
    }

    public Coiffeuse(
            String nom,
            String nomSalon,
            String email,
            String motDePasse,
            SubscriptionStatus subscriptionStatus,
            Instant abonnementActifJusquAu
    ) {
        this.nom = nom;
        this.nomSalon = nomSalon;
        this.email = email;
        this.motDePasse = motDePasse;
        this.subscriptionStatus = subscriptionStatus;
        this.abonnementActifJusquAu = abonnementActifJusquAu;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        creeLe = now;
        modifieLe = now;

        if (subscriptionStatus == null) {
            subscriptionStatus = SubscriptionStatus.TRIAL;
        }
    }

    @PreUpdate
    void preUpdate() {
        modifieLe = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getNomSalon() {
        return nomSalon;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public Instant getAbonnementActifJusquAu() {
        return abonnementActifJusquAu;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public String getModeleSmsConfirmation() {
        return modeleSmsConfirmation;
    }

    public String getModeleSmsModification() {
        return modeleSmsModification;
    }

    public String getModeleSmsRappel() {
        return modeleSmsRappel;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public Instant getModifieLe() {
        return modifieLe;
    }

    public boolean hasActiveSubscription() {
        if (subscriptionStatus == SubscriptionStatus.ACTIVE) {
            return true;
        }

        return subscriptionStatus == SubscriptionStatus.TRIAL
                && abonnementActifJusquAu != null
                && abonnementActifJusquAu.isAfter(Instant.now());
    }

    public void changerMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void activerAbonnement(String stripeCustomerId, String stripeSubscriptionId, Instant abonnementActifJusquAu) {
        this.subscriptionStatus = SubscriptionStatus.ACTIVE;
        this.stripeCustomerId = stripeCustomerId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.abonnementActifJusquAu = abonnementActifJusquAu;
    }

    public void marquerAbonnementInactif(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public void mettreAJourAbonnement(SubscriptionStatus subscriptionStatus, Instant abonnementActifJusquAu) {
        this.subscriptionStatus = subscriptionStatus;
        this.abonnementActifJusquAu = abonnementActifJusquAu;

        if (subscriptionStatus != SubscriptionStatus.ACTIVE) {
            this.stripeCustomerId = null;
            this.stripeSubscriptionId = null;
        }
    }

    public void modifierModelesSms(
            String modeleSmsConfirmation,
            String modeleSmsModification,
            String modeleSmsRappel
    ) {
        this.modeleSmsConfirmation = modeleSmsConfirmation;
        this.modeleSmsModification = modeleSmsModification;
        this.modeleSmsRappel = modeleSmsRappel;
    }
}
