package com.example.application.views.login;

import com.example.application.security.OneTimeTokens;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import org.vaadin.firitin.components.button.DefaultButton;

/**
 * Asks for the username and sends a one-time login link to the user.
 */
public class OttButton extends Button {

    public OttButton(OneTimeTokens oneTimeTokens) {
        super("Request One-Time Token");
        addClickListener(e -> new Dialog("Enter your username") {{
            var username = new TextField("Username");
            username.focus();
            add(username);
            add(new DefaultButton("Request", e2 -> {
                oneTimeTokens.sendLoginLink(username.getValue());
                // The same answer whether the user exists or not
                Notification.show("If " + username.getValue() + " is a user here, a login link is on its way.");
                close();
            }));
        }}.open());
    }
}
