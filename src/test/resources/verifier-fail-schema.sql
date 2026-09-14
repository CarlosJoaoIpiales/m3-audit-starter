CREATE TABLE IF NOT EXISTS revinfo (
    rev BIGINT NOT NULL PRIMARY KEY,
    revtstmp BIGINT,
    performed_by VARCHAR(255),
    service_name VARCHAR(40)
);

-- Deliberately missing service_name -- proves AuditSchemaVerifier fails
-- closed on a stale/missing migration (AD-1, task 3.7).
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID NOT NULL PRIMARY KEY,
    entity_name VARCHAR(255),
    entity_id VARCHAR(255),
    action VARCHAR(255),
    performed_by VARCHAR(255),
    "timestamp" TIMESTAMP,
    changes CLOB,
    revision_id BIGINT
);
