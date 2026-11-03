package com.example.application.e2e;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginIT extends AbstractE2EIT {

    @Test
    public void protectedViewRedirectsToLogin() {
        profilePage().open();
        loginPage().assertIsShown();
    }

    @Test
    public void loginWithPassword() {
        loginPage().open().loginWithPassword("user", "user");
        profilePage().open().assertLoggedInAs("user");
    }

    @Test
    public void wrongPasswordIsRejected() {
        loginPage().open().loginWithPassword("user", "wrong");
        loginPage().assertLoginError();
    }

    @Test
    public void changePasswordAndSignOut() {
        loginPage().open().loginWithPassword("admin", "admin");
        var profile = profilePage().open();
        profile.changePassword("better-admin-password");
        profile.signOut();

        loginPage().open().loginWithPassword("admin", "better-admin-password");
        profilePage().open().assertLoggedInAs("admin");
        // Restore, so that the other tests can use the original password
        profilePage().changePassword("admin");
    }

    @Test
    public void loginWithOneTimeToken() {
        loginPage().open().loginWithOneTimeToken("user");
        profilePage().open().assertLoggedInAs("user");
    }

    @Test
    public void invalidOneTimeTokenIsRejected() {
        page.navigate(getUrl() + "/login/ott?token=not-a-valid-token");
        loginPage().submitOneTimeToken();
        assertThat(page).hasURL(Pattern.compile("/login\\?error"));
    }
}
