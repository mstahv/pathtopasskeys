package com.example.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import in.virit.passkeyreauth.PasskeyReauthentication;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Asks the user to confirm a sensitive action with a passkey, although they are
 * already logged in. See {@link PasskeyReauthentication}.
 */
@Route("sensitive-action")
@PageTitle("Sensitive action")
@Menu(order = 3, icon = LineAwesomeIconUrl.FINGERPRINT_SOLID)
@PermitAll
public class SensitiveActionView extends VerticalLayout {

    private final PasskeyReauthentication reauthentication;
    private final TransferLog transferLog = new TransferLog();

    public SensitiveActionView(PasskeyReauthentication reauthentication) {
        this.reauthentication = reauthentication;
        add(new H2("Confirm with a passkey"));
        add(new Paragraph("""
                You are already logged in, but for a sensitive action (like a payment in a bank app) the app \
                wants to make sure you are still the one at the keyboard, not somebody who found your laptop \
                unlocked. Clicking the button opens a dialog telling what you are about to confirm, then asks \
                for one of your passkeys with user verification, which on today's devices means a fingerprint, \
                face or the device PIN. The action only runs if the server accepts the response."""));
        add(new Paragraph("""
                Only your own passkeys are accepted and your login session stays as it is. If you logged \
                in with a one-time token, register a passkey in your profile first."""));
        add(new Button("Transfer 100 € to Bob", e -> transfer()) {{
            addThemeVariants(ButtonVariant.PRIMARY);
        }});
        add(transferLog);
    }

    private void transfer() {
        // Shows a dialog telling what is being confirmed (the passkey prompt of the
        // browser/OS can't be customized, it typically talks about signing in).
        // The callbacks run in the UI thread, so we can update the UI directly.
        reauthentication.runAfterVerification("Transfer 100 € to Bob",
                transferLog::logTransfer,
                e -> {
                    if (!e.isCancelledByUser()) {
                        Notification.show("Transfer cancelled: " + e.getMessage())
                                .addThemeVariants(NotificationVariant.ERROR);
                    }
                });
    }

    static class TransferLog extends VerticalLayout {
        TransferLog() {
            setPadding(false);
            setSpacing(false);
        }

        void logTransfer() {
            add(new Paragraph("Transferred 100 € to Bob at " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS)));
        }
    }
}
