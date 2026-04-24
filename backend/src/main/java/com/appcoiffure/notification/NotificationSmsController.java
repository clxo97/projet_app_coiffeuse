package com.appcoiffure.notification;

import java.util.List;

import com.appcoiffure.coiffeuse.Coiffeuse;
import com.appcoiffure.coiffeuse.CurrentCoiffeuseService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications-sms")
public class NotificationSmsController {

    private final NotificationSmsRepository notificationSmsRepository;
    private final CurrentCoiffeuseService currentCoiffeuseService;

    public NotificationSmsController(
            NotificationSmsRepository notificationSmsRepository,
            CurrentCoiffeuseService currentCoiffeuseService
    ) {
        this.notificationSmsRepository = notificationSmsRepository;
        this.currentCoiffeuseService = currentCoiffeuseService;
    }

    @GetMapping
    public List<NotificationSmsResponse> list(HttpServletRequest request) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireActive(request);

        return notificationSmsRepository.findTop100ByRendezVousClientCoiffeuseIdOrderByCreeLeDesc(coiffeuse.getId())
                .stream()
                .map(NotificationSmsResponse::from)
                .toList();
    }
}
