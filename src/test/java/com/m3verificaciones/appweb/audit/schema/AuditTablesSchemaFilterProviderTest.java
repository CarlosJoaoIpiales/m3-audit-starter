package com.m3verificaciones.appweb.audit.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.mapping.Table;
import org.junit.jupiter.api.Test;

/**
 * Task 3.7 — "schema filter predicate", pure unit test, no Spring context.
 */
class AuditTablesSchemaFilterProviderTest {

    private final AuditTablesSchemaFilterProvider provider = new AuditTablesSchemaFilterProvider();

    @Test
    void migrateFilter_excludesAuditLogsRevinfoAndAnyAudTable() {
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("audit_logs"))).isFalse();
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("revinfo"))).isFalse();
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("widget_aud"))).isFalse();
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("USER_AUD"))).isFalse();
    }

    @Test
    void migrateFilter_includesEveryOrdinaryTable() {
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("users"))).isTrue();
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("meters"))).isTrue();
        // "audited" contains "aud" but does not end with "_aud" -- must not
        // be excluded by an overly broad substring match.
        assertThat(provider.getMigrateFilter().includeTable(tableNamed("audited_devices"))).isTrue();
    }

    @Test
    void createDropAndValidateFilters_areUntouched() {
        Table auditLogs = tableNamed("audit_logs");
        assertThat(provider.getCreateFilter().includeTable(auditLogs)).isTrue();
        assertThat(provider.getDropFilter().includeTable(auditLogs)).isTrue();
        assertThat(provider.getValidateFilter().includeTable(auditLogs)).isTrue();
        assertThat(provider.getTruncatorFilter().includeTable(auditLogs)).isTrue();
    }

    private Table tableNamed(String name) {
        Table table = new Table("orm");
        table.setName(name);
        return table;
    }
}
