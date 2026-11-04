package com.example.application.e2e;

import com.microsoft.playwright.Page;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.DialogElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page object for the SensitiveActionView (re-authentication with a passkey).
 */
public class SensitiveActionPage {

    private final Page page;
    private final String baseUrl;

    public SensitiveActionPage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    public SensitiveActionPage open() {
        page.navigate(baseUrl + "/sensitive-action");
        return this;
    }

    /**
     * Clicks the transfer button and confirms in the dialog that opens.
     */
    public void transfer() {
        ButtonElement.getByText(page, "Transfer 100 € to Bob").click();
        var dialog = new DialogElement(page);
        dialog.assertOpen();
        ButtonElement.getByText(dialog.getLocator(), "Confirm with passkey").click();
        dialog.assertClosed();
    }

    public void assertTransferCount(int expected) {
        assertThat(page.getByText("Transferred 100 € to Bob at")).hasCount(expected);
    }

    public void assertTransferCancelled(String reason) {
        NotificationElement.getByText(page, "Transfer cancelled: " + reason).assertOpen();
    }
}
