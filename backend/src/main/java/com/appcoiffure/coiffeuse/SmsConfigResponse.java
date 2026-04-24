package com.appcoiffure.coiffeuse;

public record SmsConfigResponse(
        String provider,
        boolean twilioConfigured,
        String from,
        String modeLabel,
        String description
) {
    public static SmsConfigResponse from(String provider, String accountSid, String authToken, String from) {
        boolean twilioMode = "twilio".equalsIgnoreCase(provider);
        boolean configured = !isBlank(accountSid) && !isBlank(authToken) && !isBlank(from);

        if (twilioMode) {
            return new SmsConfigResponse(
                    "twilio",
                    configured,
                    maskPhoneNumber(from),
                    configured ? "Mode Twilio : vrais SMS" : "Mode Twilio incomplet",
                    configured
                            ? "Les SMS sont envoyes via Twilio et peuvent etre factures."
                            : "Renseigner TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN et TWILIO_FROM avant de relancer le backend."
            );
        }

        return new SmsConfigResponse(
                "console",
                false,
                null,
                "Mode console : test local",
                "Les SMS apparaissent dans les logs du backend, aucun message reel n est envoye."
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private static String maskPhoneNumber(String value) {
        if (isBlank(value)) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return trimmed;
        }

        return "***" + trimmed.substring(trimmed.length() - 4);
    }
}
