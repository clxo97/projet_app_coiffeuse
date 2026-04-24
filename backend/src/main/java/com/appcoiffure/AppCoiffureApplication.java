package com.appcoiffure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppCoiffureApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppCoiffureApplication.class, args);
    }
}
