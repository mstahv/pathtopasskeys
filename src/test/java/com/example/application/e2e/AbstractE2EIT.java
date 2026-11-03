package com.example.application.e2e;

import com.example.application.TestcontainersConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

/**
 * Base class for the browser tests. Starts the whole app (with a PostgreSQL in
 * Testcontainers) and gives each test a fresh Playwright page.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class AbstractE2EIT extends AbstractBasePlaywrightIT {

    private static final int PORT = findFreePort();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("server.port", () -> PORT);
        registry.add("vaadin.launch-browser", () -> false);
    }

    @Override
    public String getUrl() {
        return "http://localhost:" + PORT;
    }

    protected LoginPage loginPage() {
        return new LoginPage(page, getUrl());
    }

    protected ProfilePage profilePage() {
        return new ProfilePage(page, getUrl());
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
