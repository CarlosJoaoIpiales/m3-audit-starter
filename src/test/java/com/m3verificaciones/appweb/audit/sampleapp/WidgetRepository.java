package com.m3verificaciones.appweb.audit.sampleapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetRepository extends JpaRepository<Widget, Long> {
}
