package com.tpastushok.cosmocats;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

import static java.lang.String.format;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public abstract class AbstractIt {
    private static final String DOCKER_IMAGE_NAME = "postgres";
    private static final int POSTGRES_PORT = 5432;
    private static final String POSTGRES_DB = "cosmic_cats";
    private static final String POSTGRES_USER = "cosmic-cat";
    private static final String POSTGRES_PASSWORD = "cosmic-password";

    static final GenericContainer POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER = new GenericContainer(DOCKER_IMAGE_NAME)
                .withEnv("POSTGRES_DB", POSTGRES_DB)
                .withEnv("POSTGRES_USER", POSTGRES_USER)
                .withEnv("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
                .withExposedPorts(POSTGRES_PORT);
        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setupTestContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> format("jdbc:postgresql://%s:%d/cosmic_cats",
                POSTGRES_CONTAINER.getHost(), POSTGRES_CONTAINER.getMappedPort(POSTGRES_PORT)));
        registry.add("spring.datasource.username", () -> "cosmic-cat");
        registry.add("spring.datasource.password", () -> "cosmic-password");
    }
}
