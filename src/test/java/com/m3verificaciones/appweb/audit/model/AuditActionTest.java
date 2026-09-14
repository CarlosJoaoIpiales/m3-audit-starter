package com.m3verificaciones.appweb.audit.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Task 3.2 — every action value platform-audit-trail's spec and design.md's Data Model section require. */
class AuditActionTest {

    @Test
    void carriesEveryRequiredValue() {
        List<String> names = Arrays.stream(AuditAction.values()).map(Enum::name).toList();
        assertThat(names).containsExactlyInAnyOrder(
                "CREATE", "UPDATE", "DELETE", "DENIED",
                "LOGIN", "LOGOUT", "LOGIN_FAILED",
                "PASSWORD_CHANGED", "PASSWORD_RESET", "ACCOUNT_UNLOCKED",
                "SECRET_ROTATED", "EXPORT", "DOWNLINK_SENT", "ACCESS_DENIED");
    }
}
