package com.m3verificaciones.appweb.audit.sampleapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnauditedThingRepository extends JpaRepository<UnauditedThing, Long> {
}
