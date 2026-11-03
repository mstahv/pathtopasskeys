package com.example.application.e2e;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.vaadin.addons.dramafinder.element.ButtonElement;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * The user in background tasks that update the UI with push, see BackgroundTaskView.
 */
@QuarkusTest
public class BackgroundTaskIT extends AbstractE2EIT {

    @Test
    public void securityIdentityIsNotAvailableInBackgroundTask() {
        openAs("user");
        ButtonElement.getByText(page, "Problem: SecurityIdentity in the task").click();
        assertThat(page.getByText("Task failed: ContextNotActiveException")).isVisible();
    }

    @Test
    public void workaroundsKnowTheUser() {
        openAs("user");
        ButtonElement.getByText(page, "Workaround 1: read the user before the task").click();
        ButtonElement.getByText(page, "Workaround 2: AuthenticatedUser in the task").click();
        assertThat(page.getByText("Task done for user")).hasCount(2);
    }

    private void openAs(String username) {
        loginPage().open().loginWithOneTimeToken(username);
        page.navigate(getUrl() + "/background");
    }
}
