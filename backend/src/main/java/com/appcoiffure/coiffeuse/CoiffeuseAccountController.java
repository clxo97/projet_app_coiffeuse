package com.appcoiffure.coiffeuse;

import com.appcoiffure.config.PasswordService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coiffeuse")
public class CoiffeuseAccountController {

    private final CurrentCoiffeuseService currentCoiffeuseService;
    private final PasswordService passwordService;
    private final String smsProvider;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFrom;

    public CoiffeuseAccountController(
            CurrentCoiffeuseService currentCoiffeuseService,
            PasswordService passwordService,
            @Value("${app.sms.provider}") String smsProvider,
            @Value("${app.sms.twilio.account-sid}") String twilioAccountSid,
            @Value("${app.sms.twilio.auth-token}") String twilioAuthToken,
            @Value("${app.sms.twilio.from}") String twilioFrom
    ) {
        this.currentCoiffeuseService = currentCoiffeuseService;
        this.passwordService = passwordService;
        this.smsProvider = smsProvider;
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioFrom = twilioFrom;
    }

    @GetMapping("/me")
    public ResponseEntity<CoiffeuseResponse> me(HttpServletRequest request) {
        return ResponseEntity.ok(CoiffeuseResponse.from(currentCoiffeuseService.requireCurrent(request)));
    }

    @GetMapping("/me/sms-config")
    public ResponseEntity<SmsConfigResponse> smsConfig(HttpServletRequest request) {
        currentCoiffeuseService.requireCurrent(request);
        return ResponseEntity.ok(SmsConfigResponse.from(smsProvider, twilioAccountSid, twilioAuthToken, twilioFrom));
    }

    @PatchMapping("/me/password")
    @Transactional
    public ResponseEntity<Void> changePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest body
    ) {
        if (body.motDePasseActuel() == null || body.nouveauMotDePasse() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (body.nouveauMotDePasse().length() < 8) {
            return ResponseEntity.badRequest().build();
        }

        Coiffeuse coiffeuse = currentCoiffeuseService.requireCurrent(request);

        if (!passwordService.matches(body.motDePasseActuel(), coiffeuse.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        coiffeuse.changerMotDePasse(passwordService.hash(body.nouveauMotDePasse()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/sms-templates")
    @Transactional
    public ResponseEntity<CoiffeuseResponse> updateSmsTemplates(
            HttpServletRequest request,
            @RequestBody SmsTemplatesRequest body
    ) {
        Coiffeuse coiffeuse = currentCoiffeuseService.requireCurrent(request);

        coiffeuse.modifierModelesSms(
                cleanTemplate(body.modeleSmsConfirmation()),
                cleanTemplate(body.modeleSmsModification()),
                cleanTemplate(body.modeleSmsRappel())
        );

        return ResponseEntity.ok(CoiffeuseResponse.from(coiffeuse));
    }

    private String cleanTemplate(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.length() > 500 ? cleaned.substring(0, 500) : cleaned;
    }
}
