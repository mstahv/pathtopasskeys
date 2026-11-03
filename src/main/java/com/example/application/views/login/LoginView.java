package com.example.application.views.login;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * No more passwords: sign in with a passkey, or with a one-time token as a fallback.
 */
@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login", autoLayout = false)
public class LoginView extends VerticalLayout {

    public LoginView(OttButton ottButton) {
        add(new H2("PathToPasskeys"));
        add(new LoginWithPasskeyButton());
        add(new Paragraph("No passkey yet, or on another device? Try this:"));
        add(ottButton);
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }
}
