package com.m3verificaciones.appweb.audit.sampleapp;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Task 3.7 — "unannotated entity writes no *_aud" (D4, AD-13: field-level
 * auditing is an explicit allowlist, never a blanket default).
 */
@SpringBootTest(classes = SampleAppApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UnauditedEntityTest {

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void unauditedEntityWrite_createsNoAudTable() throws SQLException {
        widgetService.createUnaudited("telemetry-row");

        // UnauditedThing has no @Audited annotation, so Envers must never
        // contribute an UNAUDITED_THING_AUD table for it -- unlike Widget,
        // which DOES get WIDGET_AUD (proven indirectly: the schema-filter and
        // revision-listener tests only ever find rows for Widget).
        try (var connection = dataSource.getConnection()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, "%AUD", null);
            boolean foundUnauditedAudTable = false;
            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                if (name != null && name.toUpperCase().contains("UNAUDITED")) {
                    foundUnauditedAudTable = true;
                }
            }
            assertThat(foundUnauditedAudTable)
                    .as("no *_aud table should exist for the unannotated UnauditedThing entity")
                    .isFalse();
        }
    }

    @Test
    void widgetTable_doesGetAnAudCounterpart() throws SQLException {
        widgetService.create("proves-widget-is-audited");
        try (var connection = dataSource.getConnection()) {
            ResultSet tables = connection.getMetaData().getTables(null, null, "WIDGET_AUD", null);
            assertThat(tables.next()).as("WIDGET_AUD must exist because Widget is @Audited").isTrue();
        }
    }
}
