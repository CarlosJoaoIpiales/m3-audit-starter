package com.m3verificaciones.appweb.audit.sampleapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.m3verificaciones.appweb.audit.model.AuditAction;
import com.m3verificaciones.appweb.audit.model.AuditLog;
import com.m3verificaciones.appweb.audit.repository.AuditLogRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Task 3.7 — "caller rollback drops the CRUD row but recordNow survives"
 * (AD-3).
 */
@SpringBootTest(classes = SampleAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuditLogWriterRollbackTest {

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private WidgetRepository widgetRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("bob", "n/a", "ROLE_ADMIN"));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void callerRollback_dropsBothTheEntityRowAndTheFeedRow() {
        WidgetService.SampleAppRollbackException ex = assertThrows(WidgetService.SampleAppRollbackException.class,
                () -> widgetService.createThenRollback("rolled-back-widget"));

        assertThat(widgetRepository.findById(ex.widgetId())).isEmpty();
        List<AuditLog> rows = auditLogRepository.findAll().stream()
                .filter(r -> "Widget".equals(r.getEntityName()) && String.valueOf(ex.widgetId()).equals(r.getEntityId()))
                .toList();
        assertThat(rows).isEmpty();
    }

    @Test
    void recordNow_survivesCallerRollback() {
        assertThrows(WidgetService.SampleAppRollbackException.class,
                () -> widgetService.recordLoginFailedThenRollback("attacker@example.com"));

        List<AuditLog> rows = auditLogRepository.findAll().stream()
                .filter(r -> r.getAction() == AuditAction.LOGIN_FAILED
                        && "attacker@example.com".equals(r.getEntityId()))
                .toList();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPerformedBy()).isEqualTo("attacker@example.com");
    }
}
