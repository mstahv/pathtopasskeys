package com.example.application.security;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.vaadin.firitin.components.notification.VNotification;

import java.io.IOException;

public class OTTHandler implements OneTimeTokenGenerationSuccessHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException, ServletException {
        // In a real word app this would be sent to you via email/sms
        var notification = VNotification.prominent("Check your email for login link!");
        notification.add(new VerticalLayout(){{
            add(new Paragraph("""
                    In a real world app this would be sent to you via email/SMS,
                    "but in this demo app we don't have email/SMS capabilities. 
                    Just click the link below to login (utilizes standard Spring 
                    Security generated page, but you can naturally replace that with a better 
                    looking alternative. After successful login you ought to reset your
                    password or some other authentication method.
                    """));
            add(new Paragraph("OTT:" + oneTimeToken.getTokenValue()));
            String url = "/login/ott?token=" + oneTimeToken.getTokenValue();
            add(new Anchor(url, "Click here to login"){{setRouterIgnore(true); /* This god damn wrong defualt 🤬*/}});
        }});
        notification.setDuration(20*1000);

    }
}
