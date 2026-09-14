package com.m3verificaciones.appweb.audit.event;

import com.m3verificaciones.appweb.audit.model.AuditAction;
import com.m3verificaciones.appweb.audit.model.AuditRevision;

/**
 * A single feed row, in flight. {@code revision} is set only for
 * entity-change events (published by {@link com.m3verificaciones.appweb.audit.envers.AuditRevisionListener})
 * and links the resulting {@code audit_logs} row to its Envers
 * {@code revinfo} row (AD-3, AD-12). It is {@code null} for every
 * non-entity event (LOGIN, LOGOUT, LOGIN_FAILED, PASSWORD_CHANGED,
 * PASSWORD_RESET, ACCOUNT_UNLOCKED, SECRET_ROTATED, EXPORT, DOWNLINK_SENT,
 * ACCESS_DENIED).
 */
public record AuditEvent(
        String entityName,
        String entityId,
        AuditAction action,
        String performedBy,
        String changes,
        AuditRevision revision) {

    public static AuditEvent of(String entityName, String entityId, AuditAction action, String performedBy,
            String changes) {
        return new AuditEvent(entityName, entityId, action, performedBy, changes, null);
    }
}
