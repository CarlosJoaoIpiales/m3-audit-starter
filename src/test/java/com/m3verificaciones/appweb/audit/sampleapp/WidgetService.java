package com.m3verificaciones.appweb.audit.sampleapp;

import com.m3verificaciones.appweb.audit.event.AuditEvent;
import com.m3verificaciones.appweb.audit.event.AuditEventPublisher;
import com.m3verificaciones.appweb.audit.model.AuditAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises the starter the same way a real service's own {@code @Service}
 * layer would (task 3.7).
 */
@Service
public class WidgetService {

    private final WidgetRepository widgetRepository;
    private final UnauditedThingRepository unauditedThingRepository;
    private final AuditEventPublisher auditEventPublisher;

    public WidgetService(WidgetRepository widgetRepository, UnauditedThingRepository unauditedThingRepository,
            AuditEventPublisher auditEventPublisher) {
        this.widgetRepository = widgetRepository;
        this.unauditedThingRepository = unauditedThingRepository;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public Widget create(String name) {
        return widgetRepository.save(new Widget(name));
    }

    @Transactional
    public Widget update(Long id, String newName) {
        Widget widget = widgetRepository.findById(id).orElseThrow();
        widget.setName(newName);
        return widget;
    }

    @Transactional
    public void delete(Long id) {
        widgetRepository.deleteById(id);
    }

    /** Proves a caller rollback drops the CRUD row (both the Envers *_aud row and the audit_logs feed row). */
    @Transactional
    public Widget createThenRollback(String name) {
        Widget saved = widgetRepository.save(new Widget(name));
        throw new SampleAppRollbackException(saved.getId());
    }

    /** Proves recordNow survives a caller rollback (REQUIRES_NEW, independent of the surrounding transaction). */
    @Transactional
    public void recordLoginFailedThenRollback(String attemptedEmail) {
        auditEventPublisher.recordNow(AuditEvent.of("LOGIN", attemptedEmail, AuditAction.LOGIN_FAILED,
                attemptedEmail, null));
        throw new SampleAppRollbackException(null);
    }

    @Transactional
    public UnauditedThing createUnaudited(String name) {
        return unauditedThingRepository.save(new UnauditedThing(name));
    }

    public static class SampleAppRollbackException extends RuntimeException {
        private final Long widgetId;

        public SampleAppRollbackException(Long widgetId) {
            super("intentional rollback for test");
            this.widgetId = widgetId;
        }

        public Long widgetId() {
            return widgetId;
        }
    }
}
