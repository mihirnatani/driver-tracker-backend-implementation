package com.drivertracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DrivertrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrivertrackerApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void checkSecret() {
        System.out.println("JWT SECRET LENGTH = " + secret.length());
    }
}