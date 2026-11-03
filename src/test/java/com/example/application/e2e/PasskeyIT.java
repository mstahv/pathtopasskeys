package com.example.application.e2e;

import io.quarkus.test.junit.QuarkusTest;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passkey (WebAuthn) flows, using Chromium's virtual authenticator in place of
 * a real platform authenticator or a security key.
 */
@QuarkusTest
public class PasskeyIT extends AbstractE2EIT {

    @Test
    public void registerPasskeyThenLoginWithItAndDeleteIt() {
        var authenticator = VirtualAuthenticator.attachTo(page);

        // Bootstrap: first login with the one-time token fallback
        loginPage().open().loginWithOneTimeToken("admin");
        var profile = profilePage().open();
        profile.assertLoggedInAs("admin");
        profile.assertPasskeyCount(0);

        profile.registerPasskey();
        profile.assertPasskeyCount(1);
        assertEquals(1, authenticator.credentialCount());

        profile.signOut();

        // Now log in again, this time with the passkey
        loginPage().open().loginWithPasskey();
        profile = profilePage().open();
        profile.assertLoggedInAs("admin");

        // Clean up so that tests stay independent of each other
        profile.deletePasskey();
        profile.assertPasskeyCount(0);
    }

    @Test
    public void registrationEndpointsRequireLogin() {
        var response = page.request().get(getUrl() + "/passkey/register-options",
                RequestOptions.create().setMaxRedirects(0));
        assertEquals(302, response.status());
        assertTrue(response.headers().get("location").endsWith("/login"));
    }

    @Test
    public void unregisteredPasskeyIsRejected() {
        var authenticator = VirtualAuthenticator.attachTo(page);

        // Register a passkey for a user, then remove it from the server only.
        loginPage().open().loginWithOneTimeToken("user");
        var profile = profilePage().open();
        profile.registerPasskey();
        profile.assertPasskeyCount(1);
        profile.deletePasskey();
        profile.assertPasskeyCount(0);
        profile.signOut();

        // The browser still has the credential, but the server no longer accepts it
        assertEquals(1, authenticator.credentialCount());
        var login = loginPage().open();
        login.loginWithPasskey();
        login.assertIsShown();
        profilePage().open();
        login.assertIsShown();
    }
}
