package com.m3verificaciones.appweb.audit.sampleapp;

import static org.assertj.core.api.Assertions.assertThat;

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
 * Task 3.7 — "Revision stamps actor+service; feed row carries revision_id".
 */
@SpringBootTest(classes = SampleAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuditRevisionListenerIntegrationTest {

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void authenticateAsAlice() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("alice", "n/a", "ROLE_ADMIN"));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCommittedByAlice_stampsRevisionAndWritesFeedRowLinkedToIt() {
        Widget widget = widgetService.create("valve-42");

        List<AuditLog> rows = auditLogRepository.findAll();
        AuditLog row = rows.stream()
                .filter(r -> "Widget".equals(r.getEntityName()) && String.valueOf(widget.getId()).equals(r.getEntityId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no audit_logs row for the created Widget"));

        assertThat(row.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(row.getPerformedBy()).isEqualTo("alice");
        assertThat(row.getServiceName()).isEqualTo("sample-app");
        assertThat(row.getRevisionId()).isNotNull();
    }

    @Test
    void updateCommittedByAlice_recordsUpdateAction() {
        Widget widget = widgetService.create("valve-43");
        widgetService.update(widget.getId(), "valve-43-renamed");

        List<AuditLog> updates = auditLogRepository.findAll().stream()
                .filter(r -> "Widget".equals(r.getEntityName()) && String.valueOf(widget.getId()).equals(r.getEntityId())
                        && r.getAction() == AuditAction.UPDATE)
                .toList();

        assertThat(updates).hasSize(1);
        assertThat(updates.get(0).getRevisionId()).isNotNull();
    }
}
