package com.example.application.security;

import com.example.application.views.login.LoginView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.auth.NavigationAccessControl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Enables Vaadin's view access control: the {@code @AnonymousAllowed},
 * {@code @PermitAll} and {@code @RolesAllowed} annotations on views. Users who
 * aren't signed in are taken to the {@link LoginView}.
 * <p>
 * In a Spring Boot app VaadinSecurityConfigurer sets this up. Here we do it
 * ourselves, it is just a few lines. The checks use the user and roles of the
 * HTTP request, which Quarkus resolves from the login cookie.
 */
@ApplicationScoped
public class NavigationAccessControlSetup {

    void serviceInit(@Observes ServiceInitEvent event) {
        var accessControl = new NavigationAccessControl();
        accessControl.setLoginView(LoginView.class);
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().addBeforeEnterListener(accessControl));
    }
}
