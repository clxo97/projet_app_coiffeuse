package com.appcoiffure.admin;

import java.util.List;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CoiffeuseRepository;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;
import com.appcoiffure.coiffeuse.SubscriptionStatus;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CurrentCoiffeuseService currentCoiffeuseService;
    private final CoiffeuseRepository coiffeuseRepository;

    public AdminController(
            CurrentCoiffeuseService currentCoiffeuseService,
            CoiffeuseRepository coiffeuseRepository
    ) {
        this.currentCoiffeuseService = currentCoiffeuseService;
        this.coiffeuseRepository = coiffeuseRepository;
    }

    @GetMapping("/coiffeuses")
    public ResponseEntity<List<AdminCoiffeuseResponse>> listCoiffeuses(HttpServletRequest request) {
        currentCoiffeuseService.requireAdmin(request);

        List<AdminCoiffeuseResponse> coiffeuses = coiffeuseRepository.findAll(Sort.by(Sort.Direction.DESC, "creeLe"))
                .stream()
                .map(AdminCoiffeuseResponse::from)
                .toList();

        return ResponseEntity.ok(coiffeuses);
    }

    @PatchMapping("/coiffeuses/{coiffeuseId}/subscription")
    @Transactional
    public ResponseEntity<AdminCoiffeuseResponse> updateSubscription(
            HttpServletRequest request,
            @PathVariable Long coiffeuseId,
            @RequestBody AdminUpdateSubscriptionRequest body
    ) {
        currentCoiffeuseService.requireAdmin(request);

        if (body.subscriptionStatus() == null) {
            return ResponseEntity.badRequest().build();
        }

        if ((body.subscriptionStatus() == SubscriptionStatus.ACTIVE || body.subscriptionStatus() == SubscriptionStatus.TRIAL)
                && body.abonnementActifJusquAu() == null) {
            return ResponseEntity.badRequest().build();
        }

        Coiffeuse coiffeuse = coiffeuseRepository.findById(coiffeuseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coiffeuse introuvable"));

        coiffeuse.mettreAJourAbonnement(body.subscriptionStatus(), body.abonnementActifJusquAu());

        return ResponseEntity.ok(AdminCoiffeuseResponse.from(coiffeuse));
    }
}
