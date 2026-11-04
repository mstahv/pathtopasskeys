package com.example.application.e2e;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Re-authentication with a passkey before a sensitive action, see
 * SensitiveActionView.
 */
@QuarkusTest
public class ReauthenticationIT extends AbstractE2EIT {

    @Test
    public void sensitiveActionRequiresUserVerification() {
        var authenticator = VirtualAuthenticator.attachTo(page);
        loginPage().open().loginWithOneTimeToken("user");
        profilePage().open().registerPasskey();

        var sensitiveAction = sensitiveActionPage().open();
        sensitiveAction.transfer();
        sensitiveAction.assertTransferCount(1);

        // Somebody else at the keyboard: the fingerprint/PIN check fails
        authenticator.setUserVerified(false);
        sensitiveAction.transfer();
        sensitiveAction.assertTransferCancelled("");
        sensitiveAction.assertTransferCount(1);

        // The real user again
        authenticator.setUserVerified(true);
        sensitiveAction.transfer();
        sensitiveAction.assertTransferCount(2);

        // Clean up so that tests stay independent of each other
        profilePage().open().deletePasskey();
    }

    @Test
    public void sensitiveActionNeedsAPasskey() {
        loginPage().open().loginWithOneTimeToken("admin");

        var sensitiveAction = sensitiveActionPage().open();
        sensitiveAction.transfer();
        sensitiveAction.assertTransferCancelled("You have no passkeys");
        sensitiveAction.assertTransferCount(0);
    }

    private SensitiveActionPage sensitiveActionPage() {
        return new SensitiveActionPage(page, getUrl());
    }
}
