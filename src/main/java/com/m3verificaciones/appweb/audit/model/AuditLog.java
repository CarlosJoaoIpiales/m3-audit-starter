package com.m3verificaciones.appweb.audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The chronological activity feed row (AD-3, AD-4). Schema owned by
 * user-backend's {@code 002-create-audit-core.sql} migration (AD-1) — no
 * consuming service's {@code ddl-auto=update} may create or alter this
 * table, which is exactly what {@link com.m3verificaciones.appweb.audit.schema.AuditTablesSchemaFilterProvider}
 * enforces.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private AuditAction action;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    /** Which backend wrote this row, e.g. {@code meters-backend} (AD-2, AD-12). */
    @Column(name = "service_name", length = 40)
    private String serviceName;

    /**
     * Links an entity-change feed row to its Envers {@code revinfo} row for
     * field-level drill-down (AD-3, AD-12). {@code null} for non-entity events
     * (LOGIN, LOGOUT, LOGIN_FAILED, EXPORT, DOWNLINK_SENT, ACCESS_DENIED, ...).
     */
    @Column(name = "revision_id")
    private Long revisionId;

    @PrePersist
    protected void onPrePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
