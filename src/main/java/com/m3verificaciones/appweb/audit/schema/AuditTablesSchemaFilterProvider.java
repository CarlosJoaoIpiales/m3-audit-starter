package com.m3verificaciones.appweb.audit.schema;

import java.util.Locale;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

/**
 * AD-1: excludes {@code audit_logs}, {@code revinfo} and every {@code *_aud}
 * table from the <b>migrate</b> (Hibernate's {@code ddl-auto=update}) action
 * only. {@code create}, {@code drop} and {@code validate} are untouched —
 * {@code create}/{@code drop} so the starter's own H2 {@code create-drop}
 * tests and any consuming service's H2 test slice still work without a
 * migration, and {@code validate} so a missing user-backend migration is a
 * hard boot failure in prod, never a silent reshape by whichever service
 * happens to deploy first.
 *
 * <p>Registered via {@code hibernate.hbm2ddl.schema_filter_provider} in
 * {@code AuditAutoConfiguration}.
 */
public class AuditTablesSchemaFilterProvider implements SchemaFilterProvider {

    static boolean isAuditTable(String tableName) {
        if (tableName == null) {
            return false;
        }
        String name = tableName.toLowerCase(Locale.ROOT);
        return name.equals("audit_logs") || name.equals("revinfo") || name.endsWith("_aud");
    }

    @Override
    public SchemaFilter getCreateFilter() {
        return SchemaFilter.ALL;
    }

    @Override
    public SchemaFilter getDropFilter() {
        return SchemaFilter.ALL;
    }

    @Override
    public SchemaFilter getTruncatorFilter() {
        return SchemaFilter.ALL;
    }

    @Override
    public SchemaFilter getMigrateFilter() {
        return ExcludeAuditTablesFilter.INSTANCE;
    }

    @Override
    public SchemaFilter getValidateFilter() {
        return SchemaFilter.ALL;
    }

    private static final class ExcludeAuditTablesFilter implements SchemaFilter {

        static final ExcludeAuditTablesFilter INSTANCE = new ExcludeAuditTablesFilter();

        @Override
        public boolean includeNamespace(Namespace namespace) {
            return true;
        }

        @Override
        public boolean includeTable(Table table) {
            return !isAuditTable(table.getName());
        }

        @Override
        public boolean includeSequence(Sequence sequence) {
            return true;
        }
    }
}
