package com.example.application.e2e;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.PasswordFieldElement;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the Profile view (and the sign out button in MainLayout).
 */
public class ProfilePage {

    private final Page page;
    private final String baseUrl;

    public ProfilePage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public ProfilePage open() {
        page.navigate(baseUrl + "/profile");
        return this;
    }

    public void assertLoggedInAs(String username) {
        assertThat(page).hasURL(Pattern.compile("/profile$"));
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                .setName("Profile: " + username))).isVisible();
    }

    public void changePassword(String newPassword) {
        PasswordFieldElement.getByLabel(page, "New password").setValue(newPassword);
        PasswordFieldElement.getByLabel(page, "Repeat password").setValue(newPassword);
        ButtonElement.getByText(page, "Change password").click();
        NotificationElement.getByText(page, "Password changed!").assertOpen();
    }

    public void signOut() {
        ButtonElement.getByText(page, "Sign out").click();
        // After logout, the main layout offers to sign in again
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in"))).isVisible();
    }
}
