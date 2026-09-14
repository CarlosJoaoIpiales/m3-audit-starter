package com.m3verificaciones.appweb.audit.model;

import com.m3verificaciones.appweb.audit.envers.AuditRevisionListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Custom {@code @RevisionEntity}, mapped to {@code revinfo} (AD-2). Schema
 * owned by user-backend's {@code 002-create-audit-core.sql} migration (AD-1).
 *
 * <p>Uses {@code IDENTITY} rather than Envers' default {@code REVINFO_SEQ}
 * (allocation size 50) so revision numbering never depends on every one of
 * the 7 deployable JVMs sharing the exact same sequence increment.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "revinfo")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    private Long rev;

    @RevisionTimestamp
    @Column(name = "revtstmp")
    private long timestamp;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "service_name", length = 40)
    private String serviceName;
}
