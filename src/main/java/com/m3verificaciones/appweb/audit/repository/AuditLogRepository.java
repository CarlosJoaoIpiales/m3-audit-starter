package com.m3verificaciones.appweb.audit.repository;

import com.m3verificaciones.appweb.audit.model.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deliberately a plain class over {@link EntityManager}, not a Spring Data
 * {@code JpaRepository} interface: Boot's own {@code JpaRepositoriesAutoConfiguration}
 * backs off entirely ({@code @ConditionalOnMissingBean(JpaRepositoryFactoryBean.class)})
 * the moment ANY bean of that type exists anywhere in the context — which
 * would silently stop a consuming service's own repositories from being
 * auto-configured. This keeps the starter's persistence self-contained
 * without touching that mechanism at all.
 */
public class AuditLogRepository {

    private final EntityManager entityManager;

    public AuditLogRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public AuditLog save(AuditLog auditLog) {
        entityManager.persist(auditLog);
        return auditLog;
    }

    public List<AuditLog> findAll() {
        return entityManager.createQuery("select a from AuditLog a order by a.timestamp desc", AuditLog.class)
                .getResultList();
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return page("select a from AuditLog a order by a.timestamp desc",
                "select count(a) from AuditLog a", pageable, query -> {
                });
    }

    public Page<AuditLog> findByEntityName(String entityName, Pageable pageable) {
        return page("select a from AuditLog a where a.entityName = :entityName order by a.timestamp desc",
                "select count(a) from AuditLog a where a.entityName = :entityName", pageable,
                query -> query.setParameter("entityName", entityName));
    }

    public Page<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId, Pageable pageable) {
        return page(
                "select a from AuditLog a where a.entityName = :entityName and a.entityId = :entityId "
                        + "order by a.timestamp desc",
                "select count(a) from AuditLog a where a.entityName = :entityName and a.entityId = :entityId",
                pageable,
                query -> {
                    query.setParameter("entityName", entityName);
                    query.setParameter("entityId", entityId);
                });
    }

    private Page<AuditLog> page(String jpql, String countJpql, Pageable pageable, ParameterBinder binder) {
        TypedQuery<AuditLog> query = entityManager.createQuery(jpql, AuditLog.class);
        binder.bind(query);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<AuditLog> content = query.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        binder.bind(countQuery);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @FunctionalInterface
    private interface ParameterBinder {
        void bind(jakarta.persistence.Query query);
    }
}
