package com.m3verificaciones.appweb.audit.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves "who did this" for both entity revisions (AD-2) and non-entity
 * feed events (AD-3). Consolidates the identical logic every consuming
 * service previously duplicated in its own {@code AuditorAwareImpl} /
 * {@code AccessDeniedHandlerImpl}.
 */
public class CurrentActorResolver {

    public String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}
