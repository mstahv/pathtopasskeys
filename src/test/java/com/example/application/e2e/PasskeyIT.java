package com.example.application.e2e;

import io.quarkus.test.junit.QuarkusTest;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void signUpWithPasskey() {
        VirtualAuthenticator.attachTo(page);

        loginPage().open().signUp("newbie");
        var profile = profilePage().open();
        profile.assertLoggedInAs("newbie");
        profile.assertPasskeyCount(1);
        profile.signOut();

        loginPage().open().loginWithPasskey();
        profilePage().open().assertLoggedInAs("newbie");
    }

    @Test
    public void cannotAddPasskeyToSomebodyElse() {
        // Not signed in
        assertEquals(403, postRegistration("admin"));

        // Signed in, but as another user
        loginPage().open().loginWithOneTimeToken("user");
        profilePage().open().assertLoggedInAs("user");
        assertEquals(403, postRegistration("admin"));
    }

    private int postRegistration(String username) {
        // With the cookies of the page. The guard stops this before the passkey would be checked.
        return page.request().post(getUrl() + "/q/webauthn/register?username=" + username,
                RequestOptions.create().setHeader("Content-Type", "application/json").setData("{}")).status();
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
