package com.m3verificaciones.appweb.audit.schema;

import com.m3verificaciones.appweb.audit.autoconfigure.AuditProperties;
import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.hibernate.boot.Metadata;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.springframework.beans.factory.InitializingBean;

/**
 * AD-1: runs once the {@code EntityManagerFactory} is ready and fails
 * startup — in every profile, not just prod's {@code validate} — when an
 * audit table Hibernate/Envers expects to exist (or expects a column on)
 * does not match what is actually in the database. This is what makes a
 * missing or stale migration a hard boot failure in {@code dev} too, where
 * {@code ddl-auto=update} would otherwise validate nothing for the tables
 * {@link AuditTablesSchemaFilterProvider} excludes from that action.
 *
 * <p>Depends on {@link EntityManagerFactory} in its constructor purely to
 * force Spring to build the EMF (and therefore run {@link MetadataCapturingIntegrator})
 * before this bean's {@link #afterPropertiesSet()} executes.
 */
public class AuditSchemaVerifier implements InitializingBean {

    private final DataSource dataSource;
    private final AuditProperties properties;

    public AuditSchemaVerifier(DataSource dataSource, EntityManagerFactory entityManagerFactory,
            AuditProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Metadata metadata = MetadataCapturingIntegrator.getCapturedMetadata();
        if (metadata == null) {
            // No Integrator callback ran -- this service maps no entities at
            // all yet (unlikely, but not this verifier's problem) or Envers
            // itself never bootstrapped. Nothing to compare.
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            for (PersistentClass persistentClass : metadata.getEntityBindings()) {
                Table table = persistentClass.getTable();
                if (AuditTablesSchemaFilterProvider.isAuditTable(table.getName())) {
                    verifyTable(connection, table);
                }
            }
        }
    }

    private void verifyTable(Connection connection, Table table) throws SQLException {
        Set<String> actualColumns = readActualColumns(connection, table.getName());
        if (actualColumns.isEmpty()) {
            throw new IllegalStateException(
                    "Audit schema verification failed: table '" + table.getName() + "' does not exist. "
                            + properties.getMigrationHint());
        }
        List<String> missing = new ArrayList<>();
        for (Column column : table.getColumns()) {
            if (!actualColumns.contains(column.getName().toLowerCase(Locale.ROOT))) {
                missing.add(column.getName());
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Audit schema verification failed: table '" + table.getName() + "' is missing column(s) "
                            + missing + ". " + properties.getMigrationHint());
        }
    }

    private Set<String> readActualColumns(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        Set<String> columns = collectColumns(meta, tableName);
        if (columns.isEmpty()) {
            // Some JDBC drivers (H2 in default mode, among others) report the
            // catalog's identifiers upper-cased regardless of how the DDL was
            // written.
            columns = collectColumns(meta, tableName.toUpperCase(Locale.ROOT));
        }
        return columns;
    }

    private Set<String> collectColumns(DatabaseMetaData meta, String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }
}
