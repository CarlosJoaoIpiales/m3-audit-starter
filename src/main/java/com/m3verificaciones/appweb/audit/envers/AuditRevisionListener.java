package com.m3verificaciones.appweb.audit.envers;

import com.m3verificaciones.appweb.audit.model.AuditAction;
import com.m3verificaciones.appweb.audit.model.AuditRevision;
import com.m3verificaciones.appweb.audit.support.CurrentActorResolver;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.context.ApplicationEventPublisher;

import com.m3verificaciones.appweb.audit.event.AuditEvent;

/**
 * Envers instantiates {@code @RevisionEntity(listener)} classes by
 * reflection (a bare no-arg constructor), so this is never itself a Spring
 * bean — {@link #configure} is called once from {@code AuditAutoConfiguration}
 * during context refresh, before any entity write can occur, to hand it the
 * collaborators it needs (AD-2, AD-3).
 *
 * <p>{@link #newRevision(Object)} stamps the {@code revinfo} row with the
 * current actor and service name. {@link #entityChanged} runs during flush
 * (before commit) and publishes an {@link AuditEvent} that
 * {@code AuditLogWriter} only turns into an {@code audit_logs} row
 * {@code AFTER_COMMIT} — so a caller rollback drops both the Envers
 * {@code *_aud} row (same transaction) and the feed row (event never
 * committed).
 */
public class AuditRevisionListener implements EntityTrackingRevisionListener {

    private static volatile ApplicationEventPublisher eventPublisher;
    private static volatile CurrentActorResolver actorResolver;
    private static volatile String serviceName;

    public static void configure(ApplicationEventPublisher publisher, CurrentActorResolver resolver,
            String service) {
        eventPublisher = publisher;
        actorResolver = resolver;
        serviceName = service;
    }

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevision revision = (AuditRevision) revisionEntity;
        revision.setPerformedBy(actorResolver != null ? actorResolver.currentActor() : "SYSTEM");
        revision.setServiceName(serviceName != null ? serviceName : "unknown");
    }

    @Override
    public void entityChanged(Class entityClass, String entityName, Object entityId, RevisionType revisionType,
            Object revisionEntity) {
        if (eventPublisher == null) {
            return;
        }
        AuditRevision revision = (AuditRevision) revisionEntity;
        AuditAction action = mapAction(revisionType);
        String simpleName = entityName != null ? simpleName(entityName) : entityClass.getSimpleName();
        eventPublisher.publishEvent(new AuditEvent(simpleName, String.valueOf(entityId), action,
                revision.getPerformedBy(), null, revision));
    }

    private String simpleName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }

    private AuditAction mapAction(RevisionType type) {
        return switch (type) {
            case ADD -> AuditAction.CREATE;
            case MOD -> AuditAction.UPDATE;
            case DEL -> AuditAction.DELETE;
        };
    }

    /** Test-only: restores a clean slate between sample-app test classes. */
    static void reset() {
        eventPublisher = null;
        actorResolver = null;
        serviceName = null;
    }
}
