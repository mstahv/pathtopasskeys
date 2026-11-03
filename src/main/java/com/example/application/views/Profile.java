package com.example.application.views;

import com.example.application.data.User;
import com.example.application.data.WebAuthnRecord;
import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.firitin.components.button.DeleteButton;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@Route
@Menu(order = 2, icon = LineAwesomeIconUrl.USER)
@PermitAll
public class Profile extends VerticalLayout {

    public Profile(AuthenticatedUser authenticatedUser) {
        User user = authenticatedUser.get().orElseThrow();
        add(new H1("Profile: " + user.getUsername()));
        add(new Paragraph("Full name: " + user.getName()));

        add(new H3("Your Roles:"));
        user.getRoles().forEach(role -> add(new Paragraph(role.name())));

        add(new H3("Passkeys:"));
        add(new PasskeyList(authenticatedUser));
    }

    /**
     * The passkeys of the current user, with buttons to register a new one or
     * to delete an existing one.
     */
    static class PasskeyList extends VerticalLayout {
        private final AuthenticatedUser authenticatedUser;

        PasskeyList(AuthenticatedUser authenticatedUser) {
            this.authenticatedUser = authenticatedUser;
            setPadding(false);
            refresh();
        }

        private void refresh() {
            removeAll();
            List<WebAuthnRecord> passkeys = authenticatedUser.getPasskeys();
            if (passkeys.isEmpty()) {
                add(new Paragraph("No passkeys registered"));
            }
            passkeys.forEach(passkey -> add(new HorizontalLayout(
                    new Paragraph(passkey.label),
                    new DeleteButton(() -> {
                        authenticatedUser.deletePasskey(passkey);
                        refresh();
                    }) {{
                        setAriaLabel("Delete passkey " + passkey.label);
                    }}) {{
                setAlignItems(Alignment.BASELINE);
            }}));
            add(new Button("Register new passkey", e -> authenticatedUser.registerPasskey()
                    .thenRun(() -> {
                        Notification.show("Passkey registered!");
                        refresh();
                    })
                    .exceptionally(ex -> {
                        Notification.show("Passkey registration failed: " + ex.getMessage())
                                .addThemeVariants(NotificationVariant.ERROR);
                        return null;
                    })));
        }
    }
}
