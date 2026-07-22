package com.baito.my_app.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

/**
 * Base class for persistence-layer integration tests.
 *
 * <p>Runs a real MySQL (the same engine as production) in a Docker container via Testcontainers,
 * so the whole path — persistence adapter → Spring Data repository → Hibernate/MySQL dialect,
 * unique constraints, {@code IDENTITY} id generation, {@code @CreationTimestamp} — is exercised
 * for real, not against an in-memory H2 that would behave differently.
 *
 * <p>{@code @DataJpaTest} loads only the JPA slice and wraps each test in a transaction that rolls
 * back, so tests stay isolated without manual cleanup. The {@code @Component} adapters are not part
 * of the slice, so each concrete test {@code @Import}s the adapter under test.
 *
 * <p>A single container is shared across every subclass (started once in a static initializer and
 * left running for the JVM; Testcontainers' Ryuk sidecar removes it on exit), which is much faster
 * than starting one per class.
 *
 * <p><b>Requires a running Docker daemon.</b>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class PersistenceTestSupport {

    @SuppressWarnings("resource")
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.0")
            .withDatabaseName("baito")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        // Let Hibernate create the schema in the fresh container from the entity mappings.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
