package com.appcoiffure.notification;

import java.time.Instant;

import com.appcoiffure.rendezvous.RendezVous;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications_sms")
public class NotificationSms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rendez_vous_id", nullable = false)
    private RendezVous rendezVous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus statut;

    @Column(nullable = false)
    private Instant dateEnvoiPrevue;

    private Instant dateEnvoi;

    @Column(nullable = false, length = 40)
    private String telephone;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 1000)
    private String erreur;

    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    protected NotificationSms() {
    }

    public NotificationSms(
            RendezVous rendezVous,
            NotificationType type,
            Instant dateEnvoiPrevue,
            String telephone,
            String message
    ) {
        this.rendezVous = rendezVous;
        this.type = type;
        this.statut = NotificationStatus.A_ENVOYER;
        this.dateEnvoiPrevue = dateEnvoiPrevue;
        this.telephone = telephone;
        this.message = message;
    }

    @PrePersist
    void prePersist() {
        creeLe = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public RendezVous getRendezVous() {
        return rendezVous;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationStatus getStatut() {
        return statut;
    }

    public Instant getDateEnvoiPrevue() {
        return dateEnvoiPrevue;
    }

    public Instant getDateEnvoi() {
        return dateEnvoi;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getMessage() {
        return message;
    }

    public String getErreur() {
        return erreur;
    }

    public Instant getCreeLe() {
        return creeLe;
    }

    public void marquerEnvoyee() {
        statut = NotificationStatus.ENVOYEE;
        dateEnvoi = Instant.now();
        erreur = null;
    }

    public void marquerEchec(String erreur) {
        statut = NotificationStatus.ECHEC;
        this.erreur = erreur;
    }
}
