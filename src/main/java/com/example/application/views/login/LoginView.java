package com.example.application.views.login;

import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(OttButton ottButton, AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        add(new H1("PathToPasskeys : Login"));
        add(new LoginWithPasskeyButton());

        add(new Paragraph("Problems signing in? Try this:"));
        add(ottButton);
        setAlignItems(Alignment.CENTER);
    }
}
