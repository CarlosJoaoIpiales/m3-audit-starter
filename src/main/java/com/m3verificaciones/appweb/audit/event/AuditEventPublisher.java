package com.m3verificaciones.appweb.audit.event;

import org.springframework.context.ApplicationEventPublisher;

/**
 * The two non-entity feed-write modes (AD-3):
 *
 * <ul>
 *   <li>{@link #recordAfterCommit(AuditEvent)} — for events tied to a
 *       mutation that must not be recorded unless the mutation actually
 *       commits (LOGIN, PASSWORD_CHANGED, PASSWORD_RESET, ACCOUNT_UNLOCKED,
 *       SECRET_ROTATED, DOWNLINK_SENT).</li>
 *   <li>{@link #recordNow(AuditEvent)} — for events that must survive a
 *       caller rollback, or that have no surrounding business transaction at
 *       all (LOGIN_FAILED, ACCESS_DENIED, EXPORT, LOGOUT).</li>
 * </ul>
 */
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditLogWriter auditLogWriter;

    public AuditEventPublisher(ApplicationEventPublisher applicationEventPublisher, AuditLogWriter auditLogWriter) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.auditLogWriter = auditLogWriter;
    }

    public void recordAfterCommit(AuditEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    public void recordNow(AuditEvent event) {
        auditLogWriter.writeNow(event);
    }
}
