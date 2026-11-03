package com.example.application.views.login;

import com.example.application.security.WebAuthnJs;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Creates an account with a passkey: anybody can sign up in this demo, and gets
 * the USER role.
 */
class SignUpForm extends HorizontalLayout {

    private final TextField username = new TextField() {{
        setPlaceholder("Username");
        setAriaLabel("Username for the new account");
        setPattern("[a-zA-Z0-9._-]+");
        setRequired(true);
    }};

    SignUpForm() {
        add(username, new Button("Sign up with passkey", e -> signUp()));
        setAlignItems(Alignment.BASELINE);
    }

    private void signUp() {
        if (username.isEmpty() || username.isInvalid()) {
            username.setInvalid(true);
            return;
        }
        WebAuthnJs.register(username.getValue())
                // Quarkus signed the new user in, reload to pick up the login
                .thenRun(() -> UI.getCurrent().getPage().setLocation("/"))
                .exceptionally(ex -> {
                    Notification.show("Sign-up failed, perhaps the username is taken?")
                            .addThemeVariants(NotificationVariant.ERROR);
                    return null;
                });
    }
}
