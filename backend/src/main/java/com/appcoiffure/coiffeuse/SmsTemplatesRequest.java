package com.appcoiffure.coiffeuse;

public record SmsTemplatesRequest(
        String modeleSmsConfirmation,
        String modeleSmsModification,
        String modeleSmsRappel
) {
}
