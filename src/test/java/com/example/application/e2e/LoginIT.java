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
    public void passwordLoginIsGone() {
        // Spring Security still has its form login filter, but it accepts no password
        // anymore, not even the old one or an empty one
        loginPage().open();
        for (String password : new String[]{"user", ""}) {
            page.evaluate("""
                    async password => {
                        const csrf = document.querySelector('meta[name=_csrf]').content;
                        const body = new URLSearchParams({username: 'user', password, _csrf: csrf});
                        await fetch('/login', {method: 'POST', body});
                    }""", password);
            profilePage().open();
            loginPage().assertIsShown();
        }
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
