package com.appcoiffure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleSmsSender implements SmsSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSmsSender.class);

    @Override
    public void send(String phoneNumber, String message) {
        LOGGER.info("SMS console vers {} : {}", phoneNumber, message);
    }
}
