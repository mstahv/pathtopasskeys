package com.example.application.views.login;

import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

public class LoginWithPasskeyButton extends Button {
    public LoginWithPasskeyButton() {
        setText("Use passkey");
        addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        AuthenticatedUser.loadWebauthJs();
        addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("window.authenticateOrError();");
        });
    }
}
