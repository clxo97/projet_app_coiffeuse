package com.appcoiffure.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;
import com.appcoiffure.rendezvous.RendezVous;
import com.appcoiffure.rendezvous.RendezVousRepository;
import com.appcoiffure.rendezvous.RendezVousResponse;
import com.appcoiffure.rendezvous.StatutRendezVous;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final RendezVousRepository rendezVousRepository;
    private final CurrentCoiffeuseService currentCoiffeuseService;

    public DashboardController(
            RendezVousRepository rendezVousRepository,
            CurrentCoiffeuseService currentCoiffeuseService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.currentCoiffeuseService = currentCoiffeuseService;
    }

    @GetMapping
    public DashboardSummaryResponse summary(HttpServletRequest request, @RequestParam(required = false) LocalDate date) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);
        LocalDate selectedDate = date == null ? LocalDate.now() : date;
        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

        List<RendezVous> todayAppointments = rendezVousRepository.findTop100ByClientCoiffeuseIdAndDateHeureBetweenOrderByDateHeureAsc(
                coiffeuse.getId(),
                start,
                end
        );

        long completedCount = todayAppointments.stream()
                .filter(rendezVous -> rendezVous.getStatut() == StatutRendezVous.TERMINE)
                .count();

        BigDecimal turnover = todayAppointments.stream()
                .filter(rendezVous -> rendezVous.getStatut() == StatutRendezVous.TERMINE)
                .map(RendezVous::getPrix)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RendezVousResponse> nextAppointments = rendezVousRepository
                .findTop5ByClientCoiffeuseIdAndDateHeureGreaterThanEqualAndStatutNotOrderByDateHeureAsc(
                        coiffeuse.getId(),
                        LocalDateTime.now(),
                        StatutRendezVous.ANNULE
                )
                .stream()
                .map(RendezVousResponse::from)
                .toList();

        List<RendezVousResponse> pendingReminders = rendezVousRepository
                .findTop5ByClientCoiffeuseIdAndDateHeureGreaterThanEqualAndStatutNotAndRappelEnvoyeFalseOrderByDateHeureAsc(
                        coiffeuse.getId(),
                        LocalDateTime.now(),
                        StatutRendezVous.ANNULE
                )
                .stream()
                .map(RendezVousResponse::from)
                .toList();

        return new DashboardSummaryResponse(
                todayAppointments.size(),
                completedCount,
                turnover,
                nextAppointments,
                pendingReminders
        );
    }
}
