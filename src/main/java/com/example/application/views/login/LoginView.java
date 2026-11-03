package com.example.application.views.login;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * The classic login screen: username and password, posted to Spring Security.
 */
@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login", autoLayout = false)
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    public LoginView(OttButton ottButton) {
        setAction("login");
        setTitle("PathToPasskeys");
        setDescription("Login using user/user or admin/admin");
        setForgotPasswordButtonVisible(false);
        getFooter().add(new VerticalLayout(
                new LoginWithPasskeyButton(),
                new Paragraph("Problems signing in? Try this:"),
                ottButton) {{
            setAlignItems(Alignment.CENTER);
        }});
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
