package com.appcoiffure.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;
import com.appcoiffure.rendezvous.RendezVous;
import com.appcoiffure.rendezvous.RendezVousRepository;
import com.appcoiffure.rendezvous.StatutRendezVous;

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
@RequestMapping("/api/finance")
public class FinanceController {

    private final RendezVousRepository rendezVousRepository;
    private final DepenseRepository depenseRepository;
    private final CurrentCoiffeuseService currentCoiffeuseService;

    public FinanceController(
            RendezVousRepository rendezVousRepository,
            DepenseRepository depenseRepository,
            CurrentCoiffeuseService currentCoiffeuseService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.depenseRepository = depenseRepository;
        this.currentCoiffeuseService = currentCoiffeuseService;
    }

    @GetMapping
    public FinanceSummaryResponse summary(
            HttpServletRequest request,
            @RequestParam(required = false) LocalDate debut,
            @RequestParam(required = false) LocalDate fin
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);
        LocalDate today = LocalDate.now();
        LocalDate selectedStart = debut == null ? today.withDayOfMonth(1) : debut;
        LocalDate selectedEnd = fin == null ? today : fin;

        if (selectedEnd.isBefore(selectedStart)) {
            selectedEnd = selectedStart;
        }

        List<RendezVous> appointments = rendezVousRepository.findByClientCoiffeuseIdAndStatutAndDateHeureBetweenOrderByDateHeureAsc(
                coiffeuse.getId(),
                StatutRendezVous.TERMINE,
                selectedStart.atStartOfDay(),
                selectedEnd.plusDays(1).atStartOfDay()
        );

        BigDecimal turnover = appointments.stream()
                .map(RendezVous::getPrix)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTicket = appointments.isEmpty()
                ? BigDecimal.ZERO
                : turnover.divide(BigDecimal.valueOf(appointments.size()), 2, RoundingMode.HALF_UP);

        List<Depense> expenses = depenseRepository.findByCoiffeuseIdAndDateDepenseBetweenOrderByDateDepenseAsc(
                coiffeuse.getId(),
                selectedStart,
                selectedEnd
        );
        BigDecimal totalExpenses = expenses.stream()
                .map(Depense::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<FinanceLineResponse> lines = appointments.stream()
                .map(appointment -> new FinanceLineResponse(
                        appointment.getId(),
                        appointment.getClient().getId(),
                        appointment.getClient().getNom(),
                        appointment.getDateHeure(),
                        appointment.getPrestation(),
                        appointment.getPrix()
                ))
                .toList();

        return new FinanceSummaryResponse(
                selectedStart,
                selectedEnd,
                turnover,
                totalExpenses,
                turnover.subtract(totalExpenses),
                appointments.size(),
                averageTicket,
                lines,
                expenses.stream().map(DepenseResponse::from).toList()
        );
    }

    @PostMapping("/depenses")
    public ResponseEntity<DepenseResponse> createExpense(HttpServletRequest httpRequest, @RequestBody DepenseRequest request) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);

        if (!isValidExpense(request)) {
            return ResponseEntity.badRequest().build();
        }

        Depense depense = new Depense(
                coiffeuse,
                request.dateDepense(),
                cleanRequired(request.libelle()),
                cleanRequired(request.categorie()),
                request.montant(),
                cleanOptional(request.notes())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(DepenseResponse.from(depenseRepository.save(depense)));
    }

    @PutMapping("/depenses/{id}")
    @Transactional
    public ResponseEntity<DepenseResponse> updateExpense(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @RequestBody DepenseRequest request
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(httpRequest);

        if (!isValidExpense(request)) {
            return ResponseEntity.badRequest().build();
        }

        Depense depense = depenseRepository.findByIdAndCoiffeuseId(id, coiffeuse.getId()).orElse(null);

        if (depense == null) {
            return ResponseEntity.notFound().build();
        }

        depense.modifier(
                request.dateDepense(),
                cleanRequired(request.libelle()),
                cleanRequired(request.categorie()),
                request.montant(),
                cleanOptional(request.notes())
        );

        return ResponseEntity.ok(DepenseResponse.from(depense));
    }

    @DeleteMapping("/depenses/{id}")
    public ResponseEntity<Void> deleteExpense(HttpServletRequest request, @PathVariable Long id) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);

        if (!depenseRepository.existsByIdAndCoiffeuseId(id, coiffeuse.getId())) {
            return ResponseEntity.notFound().build();
        }

        depenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isValidExpense(DepenseRequest request) {
        return request.dateDepense() != null
                && !isBlank(request.libelle())
                && !isBlank(request.categorie())
                && request.montant() != null
                && request.montant().compareTo(BigDecimal.ZERO) >= 0;
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
}
