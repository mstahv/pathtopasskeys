package com.example.application.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.DialogElement;
import org.vaadin.addons.dramafinder.element.MenuBarElement;
import org.vaadin.addons.dramafinder.element.MenuElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the Profile view (and the user menu in MainLayout).
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

    public void registerPasskey() {
        ButtonElement.getByText(page, "Register new passkey").click();
        NotificationElement.getByText(page, "Passkey registered!").assertOpen();
    }

    public void assertPasskeyCount(int expected) {
        assertThat(deleteButtons()).hasCount(expected);
        if (expected == 0) {
            assertThat(page.getByText("No passkeys registered")).isVisible();
        }
    }

    public void deletePasskey() {
        new ButtonElement(deleteButtons().first()).click();
        var confirmDialog = new DialogElement(page);
        confirmDialog.assertOpen();
        ButtonElement.getByText(confirmDialog.getLocator(), "Delete").click();
        confirmDialog.assertClosed();
    }

    public void signOut(String fullName) {
        // The user menu item also contains the avatar, so match on a substring of its name
        new MenuBarElement(page).getLocator()
                .getByRole(AriaRole.MENUITEM, new Locator.GetByRoleOptions().setName(fullName))
                .click();
        new MenuElement(page).getMenuItemElement("Sign out").click();
        // After logout, the main layout offers to sign in again
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in"))).isVisible();
    }

    private Locator deleteButtons() {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete passkey"));
    }
}
