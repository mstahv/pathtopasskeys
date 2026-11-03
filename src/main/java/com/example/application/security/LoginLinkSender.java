package com.example.application.security;

import com.example.application.data.User;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.enterprise.context.ApplicationScoped;
import org.vaadin.firitin.components.notification.VNotification;

/**
 * Delivers a one-time login link to the user. <b>This is the hook to replace
 * in a real app</b>: send the link by email or SMS instead of showing it.
 * <p>
 * For example with the Quarkus Mailer extension (quarkus-mailer), and an email
 * address added to {@link User}:
 * <pre>{@code
 * @Inject
 * Mailer mailer;
 *
 * @ConfigProperty(name = "app.base-url") // e.g. https://example.com
 * String baseUrl;
 *
 * public void send(User user, String link) {
 *     mailer.send(Mail.withText(user.getEmail(), "Sign in to PathToPasskeys",
 *             "Sign in with this link, it is valid for 5 minutes: " + baseUrl + link));
 * }
 * }</pre>
 * Note that the link is relative: in an email or SMS it needs the public
 * address of the app in front of it. Use a configured address, not one derived
 * from the request, which an attacker could fake to steal tokens.
 * <p>
 * The method is called while handling the request of the user who asked for
 * the link, so a slow email server slows down the UI. With
 * {@code mailer.sendAsync(...)} or {@code ReactiveMailer} the UI doesn't wait.
 */
@ApplicationScoped
public class LoginLinkSender {

    /**
     * @param user the user who asked for the link
     * @param link the path and query of the login link, e.g. {@code /login/ott?token=...}
     */
    public void send(User user, String link) {
        // This demo has no email or SMS, so we just show the link
        var notification = VNotification.prominent("Check your email for login link!");
        notification.add(new VerticalLayout() {{
            add(new Paragraph("""
                    In a real world app this would be sent to you via email/SMS, \
                    but in this demo app we don't have email/SMS capabilities. \
                    Just click the link below to login."""));
            add(new Anchor(link, "Click here to login") {{
                setRouterIgnore(true); // a plain HTTP endpoint, not a Vaadin view
            }});
        }});
        notification.setDuration(20 * 1000);
    }
}
