package com.example.application.views.login;

import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Code;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
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
        add(new DemoAccountsHint());
        setAlignItems(Alignment.CENTER);
    }

    /**
     * This is a demo app without user registration, so tell how to get in.
     */
    static class DemoAccountsHint extends Div {
        DemoAccountsHint() {
            add(new H4("Demo accounts"));
            add(new Paragraph(new Text("Use "), new Code("user"), new Text(" or "), new Code("admin"),
                    new Text(". Log in first with a one-time token, then register a passkey in the Profile view. " +
                            "After that, \"Use passkey\" works.")));
            getStyle()
                    .setMaxWidth("28em")
                    .setPadding("var(--lumo-space-s) var(--lumo-space-m)")
                    .setBorderRadius("var(--lumo-border-radius-m)")
                    .setBackground("var(--lumo-contrast-5pct)")
                    .setColor("var(--lumo-secondary-text-color)");
        }
    }
}
