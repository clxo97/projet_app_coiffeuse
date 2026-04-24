package com.appcoiffure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsSenderConfig {

    @Bean
    public SmsSender smsSender(
            @Value("${app.sms.provider}") String provider,
            @Value("${app.sms.twilio.account-sid}") String accountSid,
            @Value("${app.sms.twilio.auth-token}") String authToken,
            @Value("${app.sms.twilio.from}") String from
    ) {
        if ("twilio".equalsIgnoreCase(provider)) {
            validateTwilioConfig(accountSid, authToken, from);
            return new TwilioSmsSender(accountSid, authToken, from);
        }

        return new ConsoleSmsSender();
    }

    private void validateTwilioConfig(String accountSid, String authToken, String from) {
        if (isBlank(accountSid) || isBlank(authToken) || isBlank(from)) {
            throw new IllegalStateException(
                    "Configuration Twilio incomplete : renseigner TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_FROM"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}
