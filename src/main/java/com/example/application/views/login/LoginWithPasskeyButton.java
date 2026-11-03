package com.example.application.views.login;

import com.example.application.security.WebAuthnJs;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class LoginWithPasskeyButton extends Button {
    public LoginWithPasskeyButton() {
        setText("Use passkey");
        addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addClickListener(e -> WebAuthnJs.authenticate());
    }
}
