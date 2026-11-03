package com.example.application.views.login;

import com.example.application.security.OneTimeTokenLogin;
import com.example.application.security.OneTimeTokens;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.firitin.components.button.DefaultButton;
import org.vaadin.firitin.components.notification.VNotification;

/**
 * Requests a one-time token for the given username and "sends" the login link.
 */
public class OttButton extends Button {

    public OttButton(OneTimeTokens oneTimeTokens) {
        super("Request One-Time Token");
        addClickListener(e -> new Dialog("Enter your username") {{
            var username = new TextField("Username");
            username.focus();
            add(username);
            add(new DefaultButton("Request", e2 -> {
                deliver(oneTimeTokens.generate(username.getValue()));
                close();
            }));
        }}.open());
    }

    /**
     * In a real world app the link would be sent to the user via email or SMS.
     */
    private static void deliver(String token) {
        var notification = VNotification.prominent("Check your email for login link!");
        notification.add(new VerticalLayout() {{
            add(new Paragraph("""
                    In a real world app this would be sent to you via email/SMS,
                    but in this demo app we don't have email/SMS capabilities.
                    Just click the link below to login."""));
            add(new Anchor(OneTimeTokenLogin.PATH + "?token=" + token, "Click here to login") {{
                setRouterIgnore(true); // a plain HTTP endpoint, not a Vaadin view
            }});
        }});
        notification.setDuration(20 * 1000);
    }
}
