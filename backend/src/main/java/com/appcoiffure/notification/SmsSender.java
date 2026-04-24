package com.appcoiffure.notification;

public interface SmsSender {

    void send(String phoneNumber, String message);
}
