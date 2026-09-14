package com.m3verificaciones.appweb.audit.web;

import com.m3verificaciones.appweb.audit.event.AuditEvent;
import com.m3verificaciones.appweb.audit.event.AuditEventPublisher;
import com.m3verificaciones.appweb.audit.model.AuditAction;
import com.m3verificaciones.appweb.audit.support.CurrentActorResolver;

/**
 * Records {@code ACCESS_DENIED} from both denial points a consuming
 * service can have (P4, AD-3): {@code AccessDeniedHandlerImpl}, called by
 * Spring Security's filter chain for a URL-level denial, and
 * {@code GlobalExceptionHandler}'s {@code AccessDeniedException} handler,
 * for a method-level ({@code @PreAuthorize}) denial that the filter chain
 * never sees. Uses {@link AuditEventPublisher#recordNow(AuditEvent)} because
 * a denial has no business transaction of its own to attach to (and a
 * method-level denial happens after any of the caller's writes were rolled
 * back).
 */
public class AccessDeniedAuditor {

    private final AuditEventPublisher auditEventPublisher;
    private final CurrentActorResolver currentActorResolver;

    public AccessDeniedAuditor(AuditEventPublisher auditEventPublisher, CurrentActorResolver currentActorResolver) {
        this.auditEventPublisher = auditEventPublisher;
        this.currentActorResolver = currentActorResolver;
    }

    public void recordAccessDenied(String httpMethod, String requestUri) {
        String actor = currentActorResolver.currentActor();
        String changes = "{\"method\":\"" + httpMethod + "\",\"uri\":\"" + requestUri + "\"}";
        auditEventPublisher.recordNow(AuditEvent.of("ACCESS_DENIED", requestUri, AuditAction.ACCESS_DENIED, actor,
                changes));
    }
}
