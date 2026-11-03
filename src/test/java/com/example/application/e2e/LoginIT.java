package com.example.application.e2e;

import org.junit.jupiter.api.Test;
import org.vaadin.addons.dramafinder.element.ButtonElement;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Basic login flows: access control and the one-time token fallback.
 */
public class LoginIT extends AbstractE2EIT {

    @Test
    public void protectedViewRedirectsToLogin() {
        profilePage().open();
        loginPage().assertIsShown();
    }

    @Test
    public void aboutViewLeadsToLogin() {
        page.navigate(getUrl() + "/");
        ButtonElement.getByText(page, "Sign in").click();
        loginPage().assertIsShown();
    }

    @Test
    public void demoAccountsAreListedOnLoginPage() {
        loginPage().open();
        assertThat(page.getByText("Demo accounts")).isVisible();
    }

    @Test
    public void loginWithOneTimeToken() {
        var login = loginPage().open();
        login.loginWithOneTimeToken("user");

        var profile = profilePage().open();
        profile.assertLoggedInAs("user");
    }

    @Test
    public void invalidOneTimeTokenIsRejected() {
        page.navigate(getUrl() + "/login/ott?token=not-a-valid-token");
        loginPage().submitOneTimeToken();

        assertThat(page).hasURL(Pattern.compile("/login\\?error"));
        profilePage().open();
        loginPage().assertIsShown();
    }

    @Test
    public void signOut() {
        loginPage().open().loginWithOneTimeToken("user");
        var profile = profilePage().open();
        profile.assertLoggedInAs("user");

        profile.signOut();

        profilePage().open();
        loginPage().assertIsShown();
    }
}
