package com.appcoiffure.rendezvous;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.appcoiffure.client.Client;
import com.appcoiffure.client.ClientRepository;
import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;
import com.appcoiffure.notification.NotificationSmsService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rendez-vous")
public class RendezVousController {

    private final RendezVousRepository rendezVousRepository;
    private final ClientRepository clientRepository;
    private final NotificationSmsService notificationSmsService;
    private final CurrentCoiffeuseService currentCoiffeuseService;

    public RendezVousController(
            RendezVousRepository rendezVousRepository,
            ClientRepository clientRepository,
            NotificationSmsService notificationSmsService,
            CurrentCoiffeuseService currentCoiffeuseService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.clientRepository = clientRepository;
        this.notificationSmsService = notificationSmsService;
        this.currentCoiffeuseService = currentCoiffeuseService;
    }

    @GetMapping
    public List<RendezVousResponse> list(HttpServletRequest request, @RequestParam(required = false) LocalDate date) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);
        List<RendezVous> rendezVous = date == null
                ? rendezVousRepository.findTop100ByClientCoiffeuseIdOrderByDateHeureAsc(coiffeuse.getId())
                : rendezVousRepository.findTop100ByClientCoiffeuseIdAndDateHeureBetweenOrderByDateHeureAsc(
                        coiffeuse.getId(),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                );

        return rendezVous.stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<RendezVousResponse> create(HttpServletRequest httpRequest, @RequestBody RendezVousRequest request) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);
        ValidationResult validation = validate(request);

        if (!validation.valid()) {
            return ResponseEntity.badRequest().build();
        }

        Client client = clientRepository.findByIdAndCoiffeuseId(request.clientId(), coiffeuse.getId()).orElse(null);

        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        RendezVous rendezVous = new RendezVous(
                client,
                request.dateHeure(),
                sanitizeDuration(request.dureeMinutes()),
                cleanRequired(request.prestation()),
                request.prix(),
                request.statut() == null ? StatutRendezVous.PLANIFIE : request.statut(),
                cleanOptional(request.notes())
        );

        RendezVous saved = rendezVousRepository.save(rendezVous);
        notificationSmsService.programmerNotificationsCreation(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<RendezVousResponse> update(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody RendezVousRequest request
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);
        ValidationResult validation = validate(request);

        if (!validation.valid()) {
            return ResponseEntity.badRequest().build();
        }

        RendezVous rendezVous = rendezVousRepository.findByIdAndClientCoiffeuseId(id, coiffeuse.getId()).orElse(null);
        Client client = clientRepository.findByIdAndCoiffeuseId(request.clientId(), coiffeuse.getId()).orElse(null);

        if (rendezVous == null) {
            return ResponseEntity.notFound().build();
        }

        if (client == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        rendezVous.modifier(
                client,
                request.dateHeure(),
                sanitizeDuration(request.dureeMinutes()),
                cleanRequired(request.prestation()),
                request.prix(),
                request.statut() == null ? StatutRendezVous.PLANIFIE : request.statut(),
                cleanOptional(request.notes())
        );
        notificationSmsService.programmerNotificationsModification(rendezVous);

        return ResponseEntity.ok(toResponse(rendezVous));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);

        if (!rendezVousRepository.existsByIdAndClientCoiffeuseId(id, coiffeuse.getId())) {
            return ResponseEntity.notFound().build();
        }

        notificationSmsService.supprimerNotifications(id);
        rendezVousRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rappel-envoye")
    @Transactional
    public ResponseEntity<RendezVousResponse> markReminderSent(HttpServletRequest request, @PathVariable Long id) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);
        RendezVous rendezVous = rendezVousRepository.findByIdAndClientCoiffeuseId(id, coiffeuse.getId()).orElse(null);

        if (rendezVous == null) {
            return ResponseEntity.notFound().build();
        }

        rendezVous.marquerRappelEnvoye();
        return ResponseEntity.ok(toResponse(rendezVous));
    }

    private RendezVousResponse toResponse(RendezVous rendezVous) {
        return RendezVousResponse.from(rendezVous, notificationSmsService.construireMessageRappel(rendezVous));
    }

    private ValidationResult validate(RendezVousRequest request) {
        if (request.clientId() == null || request.dateHeure() == null || isBlank(request.prestation())) {
            return new ValidationResult(false);
        }

        if (request.prix() == null || request.prix().compareTo(BigDecimal.ZERO) < 0) {
            return new ValidationResult(false);
        }

        if (request.dureeMinutes() != null && (request.dureeMinutes() < 15 || request.dureeMinutes() > 480)) {
            return new ValidationResult(false);
        }

        return new ValidationResult(true);
    }

    private Integer sanitizeDuration(Integer duration) {
        return duration == null ? 60 : duration;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String cleanRequired(String value) {
        return value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record ValidationResult(boolean valid) {
    }
}
