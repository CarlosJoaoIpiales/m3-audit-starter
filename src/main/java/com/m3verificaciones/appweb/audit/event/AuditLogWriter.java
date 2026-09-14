package com.m3verificaciones.appweb.audit.event;

import com.m3verificaciones.appweb.audit.model.AuditLog;
import com.m3verificaciones.appweb.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The single place that turns an {@link AuditEvent} into an {@code audit_logs}
 * row (AD-3). Two entry points, both {@code REQUIRES_NEW} so a writer
 * failure never poisons the caller's transaction and an audit row is never
 * partially committed alongside caller data:
 *
 * <ul>
 *   <li>{@link #onAuditEvent(AuditEvent)} — {@code @TransactionalEventListener(AFTER_COMMIT)}.
 *       Used for entity CRUD (via {@link com.m3verificaciones.appweb.audit.envers.AuditRevisionListener})
 *       and other commit-tied events routed through {@link AuditEventPublisher#recordAfterCommit(AuditEvent)}.
 *       Never fires if the caller's transaction rolls back.</li>
 *   <li>{@link #writeNow(AuditEvent)} — writes immediately, independent of
 *       any surrounding transaction. Used by {@link AuditEventPublisher#recordNow(AuditEvent)}
 *       for LOGIN_FAILED, ACCESS_DENIED, EXPORT and LOGOUT, which must survive
 *       a caller rollback (or have no surrounding transaction at all).</li>
 * </ul>
 */
public class AuditLogWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogWriter.class);

    private final AuditLogRepository auditLogRepository;
    private final String serviceName;

    public AuditLogWriter(AuditLogRepository auditLogRepository, String serviceName) {
        this.auditLogRepository = auditLogRepository;
        this.serviceName = serviceName;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        write(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeNow(AuditEvent event) {
        write(event);
    }

    private void write(AuditEvent event) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityName(event.entityName());
            auditLog.setEntityId(event.entityId());
            auditLog.setAction(event.action());
            auditLog.setPerformedBy(event.performedBy());
            auditLog.setChanges(event.changes());
            auditLog.setServiceName(serviceName);
            if (event.revision() != null) {
                auditLog.setRevisionId(event.revision().getRev());
            }
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // An audit write must never break the caller's business flow --
            // same fail-open convention the pre-existing per-service
            // AuditLogEventHandler/AccessDeniedHandlerImpl used.
            log.error("Failed to write audit_logs row. Entity: {}, Action: {}", event.entityName(),
                    event.action(), e);
        }
    }
}
