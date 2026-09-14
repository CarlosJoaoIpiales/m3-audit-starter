package com.m3verificaciones.appweb.audit.schema;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Registered via {@code hibernate.integrator_provider} so {@link AuditSchemaVerifier}
 * can see the fully built boot-time {@link Metadata} — including the
 * {@code *_aud} tables Envers itself contributes to it before any
 * {@code Integrator} runs. There is no other supported way to read
 * column-level mapping metadata for dynamically-generated Envers tables
 * after the {@code EntityManagerFactory} exists.
 */
public class MetadataCapturingIntegrator implements Integrator {

    private static volatile Metadata capturedMetadata;

    static Metadata getCapturedMetadata() {
        return capturedMetadata;
    }

    /** Test-only: isolates independently-booted contexts in the same JVM. */
    static void reset() {
        capturedMetadata = null;
    }

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
            SessionFactoryImplementor sessionFactory) {
        capturedMetadata = metadata;
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        // no-op: nothing to release. Deliberately does not clear
        // capturedMetadata here -- disintegrate can run during ordinary
        // context refresh churn (e.g. test slices), and AuditSchemaVerifier
        // reads the metadata once, at its own startup.
    }
}
