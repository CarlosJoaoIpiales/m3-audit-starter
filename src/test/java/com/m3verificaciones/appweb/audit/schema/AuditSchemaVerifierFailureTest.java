package com.m3verificaciones.appweb.audit.schema;

import static org.assertj.core.api.Assertions.assertThat;

import com.m3verificaciones.appweb.audit.autoconfigure.AuditAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Task 3.7 — "verifier fails on a missing column" (AD-1's highest-risk
 * guarantee: a stale/missing migration is a hard boot failure in every
 * profile, not just prod's {@code validate}).
 */
class AuditSchemaVerifierFailureTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    SqlInitializationAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class,
                    TransactionAutoConfiguration.class,
                    AuditAutoConfiguration.class))
            .withPropertyValues(
                    "spring.datasource.url=jdbc:h2:mem:auditverifierfail;DB_CLOSE_DELAY=-1",
                    "spring.datasource.driver-class-name=org.h2.Driver",
                    "spring.jpa.hibernate.ddl-auto=none",
                    "spring.sql.init.mode=always",
                    "spring.sql.init.schema-locations=classpath:verifier-fail-schema.sql",
                    "m3.audit.service-name=verifier-fail-test");

    @Test
    void contextRefusesToStart_whenAuditLogsIsMissingAColumn() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            Throwable failure = context.getStartupFailure();
            assertThat(failure).isNotNull();
            String message = rootMessages(failure);
            assertThat(message).contains("audit_logs").contains("service_name");
        });
    }

    private String rootMessages(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(" | ");
            }
            current = current.getCause();
        }
        return sb.toString();
    }
}
