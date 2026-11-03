package com.example.application.e2e;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Passkey (WebAuthn) flows, using Chromium's virtual authenticator in place of
 * a real platform authenticator or a security key.
 */
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

        profile.signOut("Emma Executive");

        // Now log in again, this time with the passkey
        loginPage().open().loginWithPasskey();
        profile = profilePage().open();
        profile.assertLoggedInAs("admin");

        // Clean up so that tests stay independent of each other
        profile.deletePasskey();
        profile.assertPasskeyCount(0);
    }

    @Test
    public void registrationEndpointsRequireCsrfToken() {
        loginPage().open().loginWithOneTimeToken("user");
        profilePage().open().assertLoggedInAs("user");

        // Same session cookies as the page, but no CSRF token
        var response = page.request().post(getUrl() + "/webauthn/register/options");
        assertEquals(403, response.status());
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
        profile.signOut("John Normal");

        // The browser still has the credential, but the server no longer accepts it
        assertEquals(1, authenticator.credentialCount());
        var login = loginPage().open();
        login.loginWithPasskey();
        login.assertIsShown();
        profilePage().open();
        login.assertIsShown();
    }
}
