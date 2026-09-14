package com.m3verificaciones.appweb.audit.sampleapp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/** Stand-in for an {@code @Audited} entity like {@code meters-backend}'s {@code Brand} (AD-13). */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Audited
public class Widget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Widget(String name) {
        this.name = name;
    }
}
