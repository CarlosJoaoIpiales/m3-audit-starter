package com.m3verificaciones.appweb.audit.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code m3.audit.*} — every consuming service must set {@code service-name}
 * (AD-2, stamped on every {@code revinfo} and {@code audit_logs} row so
 * cross-service rows in the shared feed can be told apart).
 */
@ConfigurationProperties(prefix = "m3.audit")
public class AuditProperties {

    /** Required. Identifies this service in {@code revinfo.service_name} / {@code audit_logs.service_name}. */
    private String serviceName;

    /**
     * Included verbatim in {@link com.m3verificaciones.appweb.audit.schema.AuditSchemaVerifier}
     * failure messages, so a stale/missing migration names the fix, not just
     * the symptom (AD-1).
     */
    private String migrationHint = "Apply the owning migration in "
            + "iot-platform-user-backend/database/migrations before deploying this service "
            + "(002-create-audit-core.sql for audit_logs/revinfo, or the *-create-*-aud-tables.sql "
            + "migration owned by this service).";

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMigrationHint() {
        return migrationHint;
    }

    public void setMigrationHint(String migrationHint) {
        this.migrationHint = migrationHint;
    }
}
