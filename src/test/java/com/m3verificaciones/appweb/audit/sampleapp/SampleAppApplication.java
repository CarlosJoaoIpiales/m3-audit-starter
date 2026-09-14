package com.m3verificaciones.appweb.audit.sampleapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal host application used only by the starter's own tests (task 3.7).
 * Proves the starter behaves correctly when consumed the same way a real
 * service would: plain {@code @SpringBootApplication} package scanning plus
 * whatever {@code AuditAutoConfiguration} contributes automatically.
 */
@SpringBootApplication
public class SampleAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleAppApplication.class, args);
    }
}
