package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Route
@Menu(order = 2, icon = LineAwesomeIconUrl.USER)
@PermitAll
public class Profile extends VerticalLayout {

    public Profile(UserService userService, AuthenticatedUser authenticatedUser) {
        User user = authenticatedUser.get().orElseThrow();
        add(new H1("Profile: " + user.getUsername()));
        add(new Paragraph("Full name: " + user.getName()));

        add(new H3("Your Roles:"));
        user.getRoles().forEach(role -> add(new Paragraph(role.name())));

        add(new H3("Change password:"));
        add(new ChangePasswordForm(userService, user));
    }

    static class ChangePasswordForm extends VerticalLayout {
        private final PasswordField newPassword = new PasswordField("New password");
        private final PasswordField repeatPassword = new PasswordField("Repeat password");

        ChangePasswordForm(UserService userService, User user) {
            add(newPassword, repeatPassword, new Button("Change password", e -> {
                if (newPassword.isEmpty() || !newPassword.getValue().equals(repeatPassword.getValue())) {
                    Notification.show("Passwords do not match!").addThemeVariants(NotificationVariant.ERROR);
                } else {
                    userService.changePassword(user, newPassword.getValue());
                    Notification.show("Password changed!");
                    newPassword.clear();
                    repeatPassword.clear();
                }
            }));
            setPadding(false);
        }
    }
}
