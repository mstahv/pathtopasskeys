package com.example.application.views.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.servlet.ServletException;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.vaadin.firitin.components.button.DefaultButton;

import java.io.IOException;

@SpringComponent
@Scope("prototype")
public class OttButton extends Button {

    /**
     * A Vaadin button that requests a one-time tokens, pretty much like default Spring Security filters
     * do it.
     *
     * @param oneTimeTokenService
     * @param tokenGenerationSuccessHandler
     */
    public OttButton(OneTimeTokenService oneTimeTokenService, OneTimeTokenGenerationSuccessHandler tokenGenerationSuccessHandler) {
        super("Request One-Time Token");
        addClickListener(e -> {
            Dialog enterYourUsername = new Dialog("Enter your username") {{
                add(new Paragraph("Test app hint: there are users with username 'user' and 'admin' by default."));
                var username = new TextField("Username");
                username.focus();
                add(username);
                add(new DefaultButton("Request", e2 -> {
                    OneTimeToken token = oneTimeTokenService.generate(new GenerateOneTimeTokenRequest(username.getValue()));
                    try {
                        // This delivers to the token to end user, in this demo shows a notifications.
                        tokenGenerationSuccessHandler.handle(null, null, token);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } catch (ServletException ex) {
                        throw new RuntimeException(ex);
                    }
                    Notification.show("Token generated, check your email");
                    close();
                }));
            }};
            enterYourUsername.open();
        });
    }
}
