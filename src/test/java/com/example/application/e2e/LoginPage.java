package com.example.application.e2e;

import com.microsoft.playwright.Page;
import org.vaadin.addons.dramafinder.element.LoginOverlayElement;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the LoginView.
 */
public class LoginPage {

    private final Page page;
    private final String baseUrl;

    public LoginPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public LoginPage open() {
        page.navigate(baseUrl + "/login");
        assertIsShown();
        return this;
    }

    public void assertIsShown() {
        assertThat(page).hasURL(Pattern.compile("/login(\\?.*)?$"));
        loginOverlay().assertOpen();
    }

    public void loginWithPassword(String username, String password) {
        loginOverlay().login(username, password);
        // Wait for the form POST and redirect to complete
        page.waitForURL(url -> !url.endsWith("/login"));
    }

    public void assertLoginError() {
        loginOverlay().assertErrorVisible();
    }

    private LoginOverlayElement loginOverlay() {
        return new LoginOverlayElement(page);
    }
}
