package com.example.application.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.DialogElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextFieldElement;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the LoginView (and the one-time token submit page it
 * leads to).
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
        ButtonElement.getByText(page, "Use passkey").assertVisible();
    }

    /**
     * Requests a one-time token for the given user and returns the
     * notification that in this demo replaces the email/SMS delivery.
     */
    public NotificationElement requestOneTimeToken(String username) {
        ButtonElement.getByText(page, "Request One-Time Token").click();
        var dialog = DialogElement.getByHeaderText(page, "Enter your username");
        dialog.assertOpen();
        TextFieldElement.getByLabel(dialog.getLocator(), "Username").setValue(username);
        ButtonElement.getByText(dialog.getLocator(), "Request").click();
        dialog.assertClosed();

        var notification = NotificationElement.getByText(page, "Click here to login");
        notification.assertOpen();
        return notification;
    }

    /**
     * Full one-time token login: request the token, follow the "emailed" link
     * and submit the token.
     */
    public void loginWithOneTimeToken(String username) {
        var notification = requestOneTimeToken(username);
        notification.getLocator()
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Click here to login"))
                .click();
        submitOneTimeToken();
    }

    /**
     * Submits the token on the /login/ott page.
     */
    public void submitOneTimeToken() {
        assertThat(page).hasURL(Pattern.compile("/login/ott\\?token="));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();
        // Wait for the form POST and redirect to complete
        page.waitForURL(url -> !url.contains("/login/ott"));
    }

    /**
     * Clicks the passkey button. Requires a {@link VirtualAuthenticator} on the page.
     */
    public void loginWithPasskey() {
        ButtonElement.getByText(page, "Use passkey").click();
        // The ceremony ends with a JS redirect: to the app on success, /login?error on failure
        page.waitForURL(url -> !url.endsWith("/login"));
    }
}
