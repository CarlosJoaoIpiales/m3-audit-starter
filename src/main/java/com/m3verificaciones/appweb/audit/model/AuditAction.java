package com.m3verificaciones.appweb.audit.model;

/**
 * Every event type the shared {@code audit_logs} activity feed can carry.
 *
 * <p>{@code CREATE}, {@code UPDATE}, {@code DELETE} and legacy, read-only
 * {@code DENIED} are the pre-existing values written by the three services
 * that had their own copy of this enum before the starter existed
 * (migration {@code 002-create-audit-core.sql} does not narrow the column,
 * so old rows using {@code DENIED} keep reading back correctly). Every other
 * value is new in this change (AD-3, AD-4, AD-6, AD-9, AD-13,
 * platform-audit-trail spec "Activity Feed Event Types").
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    DENIED,
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    PASSWORD_CHANGED,
    PASSWORD_RESET,
    ACCOUNT_UNLOCKED,
    SECRET_ROTATED,
    EXPORT,
    DOWNLINK_SENT,
    ACCESS_DENIED
}
