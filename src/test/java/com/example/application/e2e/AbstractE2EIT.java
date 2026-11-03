package com.example.application.e2e;

import io.quarkus.test.common.http.TestHTTPResource;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;

import java.net.URL;

/**
 * Base class for the browser tests. Starts the whole app (with a PostgreSQL
 * from Quarkus Dev Services) and gives each test a fresh Playwright page.
 * <p>
 * The app runs on a random port. That is fine for WebAuthn too, as the
 * allowed origin is not configured for tests, so the origin of the request is used.
 * <p>
 * Note: the concrete test classes need the {@code @QuarkusTest} annotation
 * themselves, Quarkus doesn't pick it up from this superclass.
 */
public abstract class AbstractE2EIT extends AbstractBasePlaywrightIT {

    @TestHTTPResource("/")
    URL url;

    @Override
    public String getUrl() {
        return url.toString().replaceAll("/$", "");
    }

    protected LoginPage loginPage() {
        return new LoginPage(page, getUrl());
    }

    protected ProfilePage profilePage() {
        return new ProfilePage(page, getUrl());
    }
}
