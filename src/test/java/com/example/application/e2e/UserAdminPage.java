package com.example.application.e2e;

import com.microsoft.playwright.Page;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.DialogElement;
import org.vaadin.addons.dramafinder.element.GridElement;
import org.vaadin.addons.dramafinder.element.TextFieldElement;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the UserAdminView.
 */
public class UserAdminPage {

    private final Page page;
    private final String baseUrl;

    public UserAdminPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public UserAdminPage open() {
        page.navigate(baseUrl + "/users");
        GridElement.get(page).assertVisible();
        return this;
    }

    public void addUser(String username, String fullName) {
        TextFieldElement.getByLabel(page, "Username").setValue(username);
        TextFieldElement.getByLabel(page, "Full name").setValue(fullName);
        ButtonElement.getByText(page, "Add user").click();
        assertUserListed(username);
    }

    public void assertUserListed(String username) {
        assertThat(page.locator("vaadin-grid-cell-content").getByText(username, new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).isVisible();
    }

    public void assertUserNotListed(String username) {
        assertThat(page.locator("vaadin-grid-cell-content").getByText(username, new com.microsoft.playwright.Locator.GetByTextOptions().setExact(true))).hasCount(0);
    }

    public void deleteUser(String username) {
        ButtonElement.getByLabel(page, "Delete user " + username).click();
        var confirmDialog = new DialogElement(page);
        confirmDialog.assertOpen();
        ButtonElement.getByText(confirmDialog.getLocator(), "Delete").click();
        assertUserNotListed(username);
    }
}
