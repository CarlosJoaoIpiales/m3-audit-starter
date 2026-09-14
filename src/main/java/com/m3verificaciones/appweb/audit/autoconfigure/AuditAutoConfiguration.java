package com.m3verificaciones.appweb.audit.autoconfigure;

import com.m3verificaciones.appweb.audit.envers.AuditRevisionListener;
import com.m3verificaciones.appweb.audit.event.AuditEventPublisher;
import com.m3verificaciones.appweb.audit.event.AuditLogWriter;
import com.m3verificaciones.appweb.audit.model.AuditLog;
import com.m3verificaciones.appweb.audit.repository.AuditLogRepository;
import com.m3verificaciones.appweb.audit.schema.AuditSchemaVerifier;
import com.m3verificaciones.appweb.audit.schema.AuditTablesSchemaFilterProvider;
import com.m3verificaciones.appweb.audit.schema.MetadataCapturingIntegrator;
import com.m3verificaciones.appweb.audit.support.CurrentActorResolver;
import com.m3verificaciones.appweb.audit.web.AccessDeniedAuditor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

/**
 * Wires the starter into a consuming service (D7, AD-1, AD-2, AD-3).
 * Registered via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 *
 * <p>{@link AuditLogRepository} is deliberately a plain class over
 * {@link EntityManager}, not a Spring Data {@code JpaRepository} interface:
 * Boot's own {@code JpaRepositoriesAutoConfiguration} backs off entirely
 * ({@code @ConditionalOnMissingBean(JpaRepositoryFactoryBean.class)}) the
 * moment ANY bean of that type exists in the context, regardless of which
 * base package it scans — which would silently stop the consuming service's
 * own repositories from being auto-configured. Confirmed empirically: an
 * earlier attempt at this used a manual {@code JpaRepositoryFactoryBean}
 * for just this one interface, and the sample app's own {@code WidgetRepository}
 * stopped resolving as a bean at all.
 *
 * <p>{@link #auditPersistenceManagedTypes} exists for the identical reason,
 * one level lower: a plain {@code @EntityScan(basePackageClasses = AuditLog.class)}
 * was tried first and empirically broke the consuming service's OWN entity
 * scanning entirely, not just merged with it -- Boot's {@code JpaBaseConfiguration}
 * only falls back to {@code AutoConfigurationPackages} (the app's own base
 * package) when {@code EntityScanPackages} is completely empty; the moment
 * any {@code @EntityScan} exists anywhere, that fallback never runs, and only
 * the explicitly {@code @EntityScan}-registered packages get scanned.
 * Building the {@link PersistenceManagedTypes} bean by hand keeps the app's
 * own package folded in alongside this starter's.
 */
@AutoConfiguration(before = HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {

    @Bean
    public PersistenceManagedTypes auditPersistenceManagedTypes(BeanFactory beanFactory,
            ResourceLoader resourceLoader) {
        List<String> packagesToScan = new ArrayList<>();
        if (AutoConfigurationPackages.has(beanFactory)) {
            packagesToScan.addAll(AutoConfigurationPackages.get(beanFactory));
        }
        packagesToScan.add(AuditLog.class.getPackageName());
        return new PersistenceManagedTypesScanner(resourceLoader).scan(packagesToScan.toArray(new String[0]));
    }

    @Bean
    public CurrentActorResolver currentActorResolver() {
        return new CurrentActorResolver();
    }

    @Bean
    public AuditLogRepository auditLogRepository(EntityManagerFactory entityManagerFactory) {
        // No plain EntityManager bean is ever registered by Boot's JPA
        // auto-configuration (only EntityManagerFactory, resolved via
        // @PersistenceContext proxies elsewhere) -- a shared, transaction-aware
        // EntityManager has to be built explicitly here.
        EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        return new AuditLogRepository(entityManager);
    }

    @Bean
    public AuditLogWriter auditLogWriter(@Lazy AuditLogRepository auditLogRepository, AuditProperties properties) {
        return new AuditLogWriter(auditLogRepository, properties.getServiceName());
    }

    @Bean
    public AuditEventPublisher auditEventPublisher(ApplicationEventPublisher applicationEventPublisher,
            AuditLogWriter auditLogWriter) {
        return new AuditEventPublisher(applicationEventPublisher, auditLogWriter);
    }

    @Bean
    public AccessDeniedAuditor accessDeniedAuditor(AuditEventPublisher auditEventPublisher,
            CurrentActorResolver currentActorResolver) {
        return new AccessDeniedAuditor(auditEventPublisher, currentActorResolver);
    }

    // AD-2/AD-3: AuditRevisionListener is instantiated by Envers via
    // reflection (a bare @RevisionEntity(listener) no-arg constructor), so it
    // is never itself a Spring bean. This wires the static holder eagerly,
    // during context refresh, before any request can reach a transactional
    // write.
    @Bean
    public InitializingBean auditRevisionListenerConfigurer(ApplicationEventPublisher applicationEventPublisher,
            CurrentActorResolver currentActorResolver, AuditProperties properties) {
        return () -> AuditRevisionListener.configure(applicationEventPublisher, currentActorResolver,
                properties.getServiceName());
    }

    // AD-1: excludes audit_logs/revinfo/*_aud from ddl-auto=update only.
    // AD-1: also registers the Integrator that lets AuditSchemaVerifier read
    // Envers' contributed *_aud table metadata after EMF bootstrap.
    @Bean
    public HibernatePropertiesCustomizer auditHibernatePropertiesCustomizer() {
        return properties -> {
            properties.put("hibernate.hbm2ddl.schema_filter_provider",
                    AuditTablesSchemaFilterProvider.class.getName());
            properties.put("hibernate.integrator_provider",
                    (IntegratorProvider) () -> List.<Integrator>of(new MetadataCapturingIntegrator()));
        };
    }

    @Bean
    public AuditSchemaVerifier auditSchemaVerifier(DataSource dataSource, EntityManagerFactory entityManagerFactory,
            AuditProperties properties) {
        return new AuditSchemaVerifier(dataSource, entityManagerFactory, properties);
    }
}
