package com.m3verificaciones.appweb.audit.sampleapp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stand-in for a telemetry entity like {@code ConsumptionRaw} (D4, AD-13) --
 * deliberately not {@code @Audited}, so the field-level allowlist stays
 * explicit rather than a blanket default.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
public class UnauditedThing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public UnauditedThing(String name) {
        this.name = name;
    }
}
